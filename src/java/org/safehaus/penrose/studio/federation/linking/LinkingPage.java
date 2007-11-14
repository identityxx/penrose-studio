package org.safehaus.penrose.studio.federation.linking;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.federation.wizard.BrowserWizard;
import org.safehaus.penrose.studio.federation.Repository;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.SubstringFilter;
import org.safehaus.penrose.filter.FilterTool;
import org.safehaus.penrose.filter.SimpleFilter;
import org.safehaus.penrose.util.ActiveDirectoryUtil;
import org.safehaus.penrose.util.BinaryUtil;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.management.PartitionClient;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Endi S. Dewata
 */
public class LinkingPage extends FormPage {

    public Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Color red;
    Color green;
    Color blue;

    Text baseDnText;
    Text filterText;
    Combo scopeCombo;

    Table localTable;
    Table localAttributeTable;

    Table globalTable;
    Table globalAttributeTable;

    LinkingEditor editor;

    Project project;
    Repository repository;

    PartitionClient localPartitionClient;
    PartitionClient globalPartitionClient;

    DN localBaseDn;
    DN globalBaseDn;

    public LinkingPage(LinkingEditor editor) throws Exception {
        super(editor, "IDENTITY_LINKING", "  Identity Linking  ");

        this.editor = editor;
        this.project = editor.getProject();
        this.repository = editor.getRepository();

        PenroseClient penroseClient = project.getClient();

        localPartitionClient = penroseClient.getPartitionClient(repository.getName());
        globalPartitionClient = penroseClient.getPartitionClient("federation_global");

        localBaseDn = localPartitionClient.getSuffixes().iterator().next();
        globalBaseDn = globalPartitionClient.getSuffixes().iterator().next();

        Display display = Display.getDefault();

        red = display.getSystemColor(SWT.COLOR_RED);
        green = display.getSystemColor(SWT.COLOR_GREEN);
        blue = display.getSystemColor(SWT.COLOR_BLUE);
    }

    public void createFormContent(IManagedForm managedForm) {
        try {
            toolkit = managedForm.getToolkit();

            ScrolledForm form = managedForm.getForm();
            form.setText("LDAP Link");

            Composite body = form.getBody();
            body.setLayout(new GridLayout(2, false));

            Section localSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            localSection.setText("Local");

            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 2;
            localSection.setLayoutData(gd);

            Control localControl = createLocalControl(localSection);
            localSection.setClient(localControl);

            Section globalSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            globalSection.setText("Global");
            globalSection.setLayoutData(new GridData(GridData.FILL_BOTH));

            Control globalControl = createGlobalControl(globalSection);
            globalSection.setClient(globalControl);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public Composite createLocalControl(Composite parent) throws Exception {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite topPanel = createTopLocalSection(composite);
        topPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite bottomPanel = createBottomLocalSection(composite);
        bottomPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createTopLocalSection(Composite parent) throws Exception {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(3, false));

        Label baseDnLabel = toolkit.createLabel(composite, "Base DN:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        baseDnLabel.setLayoutData(gd);

        baseDnText = toolkit.createText(composite, localBaseDn.toString(), SWT.BORDER);
        baseDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button baseDnButton = toolkit.createButton(composite, "Browse", SWT.PUSH);
        gd = new GridData();
        gd.widthHint = 100;
        baseDnButton.setLayoutData(gd);

        baseDnButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    BrowserWizard wizard = new BrowserWizard();
                    wizard.setBaseDn(localBaseDn);
                    wizard.setDn(baseDnText.getText());
                    wizard.setPartitionClient(localPartitionClient);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    if (dialog.open() != Window.OK) return;

                    baseDnText.setText(wizard.getDn());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Label filterLabel = toolkit.createLabel(composite, "Filter:");
        gd = new GridData();
        gd.widthHint = 100;
        filterLabel.setLayoutData(gd);

        String filter = "(objectClass=*)";

        filterText = toolkit.createText(composite, filter, SWT.BORDER);
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(composite, SWT.NONE);
        
        Label scopeLabel = toolkit.createLabel(composite, "Scope:");
        gd = new GridData();
        gd.widthHint = 100;
        scopeLabel.setLayoutData(gd);

        String scope = "SUBTREE";

        scopeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        scopeCombo.add("OBJECT");
        scopeCombo.add("ONELEVEL");
        scopeCombo.add("SUBTREE");
        scopeCombo.setText(scope);

        gd = new GridData();
        gd.widthHint = 100;
        scopeCombo.setLayoutData(gd);

        return composite;
    }

    public Composite createBottomLocalSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        localTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 150;
        localTable.setLayoutData(gd);

        localTable.setHeaderVisible(true);
        localTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(localTable, SWT.NONE);
        tc.setText("DN");
        tc.setWidth(250);

        tc = new TableColumn(localTable, SWT.NONE);
        tc.setText("Status");
        tc.setWidth(100);

        localTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    localAttributeTable.removeAll();
                    globalTable.removeAll();
                    globalAttributeTable.removeAll();

                    if (localTable.getSelectionCount() != 1) return;

                    TableItem item = localTable.getSelection()[0];

                    SearchResult local = (SearchResult)item.getData("local");
                    updateAttributes(localAttributeTable, localPartitionClient, local.getDn());

                    updateGlobal(item);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        localAttributeTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 300;
        localAttributeTable.setLayoutData(gd);

        localAttributeTable.setHeaderVisible(true);
        localAttributeTable.setLinesVisible(true);

        tc = new TableColumn(localAttributeTable, SWT.NONE);
        tc.setText("Attribute");
        tc.setWidth(100);

        tc = new TableColumn(localAttributeTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(200);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new RowLayout());

        Button refreshButton = toolkit.createButton(buttons, "  Refresh  ", SWT.PUSH);

        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                refresh();
            }
        });

        toolkit.createLabel(buttons, "  ", SWT.NONE);

        Button linkButton = toolkit.createButton(buttons, "  Link  ", SWT.PUSH);

        linkButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                link();
            }
        });

        Button unlinkButton = toolkit.createButton(buttons, "  Unlink  ", SWT.PUSH);

        unlinkButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                unlink();
            }
        });

        toolkit.createLabel(buttons, "  ", SWT.NONE);

        Button searchButton = toolkit.createButton(buttons, "  Search  ", SWT.PUSH);

        searchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                search();
            }
        });

        Button createButton = toolkit.createButton(buttons, "  Create  ", SWT.PUSH);

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                create();
            }
        });

        return composite;
    }

    public Composite createGlobalControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        globalTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_BOTH);
        globalTable.setLayoutData(gd);

        globalTable.setHeaderVisible(true);
        globalTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(globalTable, SWT.NONE);
        tc.setText("DN");
        tc.setWidth(350);

        globalTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (globalTable.getSelectionCount() != 1) return;

                    globalAttributeTable.removeAll();

                    TableItem item = globalTable.getSelection()[0];

                    SearchResult link = (SearchResult)item.getData();
                    updateAttributes(globalAttributeTable, globalPartitionClient, link.getDn());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        globalAttributeTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 300;
        globalAttributeTable.setLayoutData(gd);

        globalAttributeTable.setHeaderVisible(true);
        globalAttributeTable.setLinesVisible(true);

        tc = new TableColumn(globalAttributeTable, SWT.NONE);
        tc.setText("Attribute");
        tc.setWidth(100);

        tc = new TableColumn(globalAttributeTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(200);

        return composite;
    }

    public Filter createFilter(String name, String s) {
        if (s == null || "".equals(s)) return null;

        Collection<Object> substrings = new ArrayList<Object>();
        substrings.add(SubstringFilter.STAR);

        StringBuilder sb = null;

        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) {
                if (sb == null) sb = new StringBuilder();
                sb.append(c);

            } else if (sb != null) {
                if (sb.length() >= 2) {
                    substrings.add(sb.toString());
                    substrings.add(SubstringFilter.STAR);
                }
                sb = null;
            }
        }

        if (sb != null) {
            substrings.add(sb.toString());
            substrings.add(SubstringFilter.STAR);
        }

        return new SubstringFilter(name, substrings);
    }

    public void refresh() {
        try {
            localTable.removeAll();
            localAttributeTable.removeAll();
            globalTable.removeAll();
            globalAttributeTable.removeAll();

            DN searchBaseDn = new DN(baseDnText.getText());
            //searchBaseDn = searchBaseDn.getPrefix(localBaseDn);

            String filter = filterText.getText();
            if ("".equals(filter)) filter = "(objectClass=*)";

            int scope = scopeCombo.getSelectionIndex();

            log.debug("Searching ["+searchBaseDn+"]");

            final SearchRequest request = new SearchRequest();
            request.setDn(searchBaseDn);
            request.setFilter(filter);
            request.setScope(scope);

            final Map<DN,SearchResult> results = new HashMap<DN,SearchResult>();
            final Map<DN,Collection<SearchResult>> links = new HashMap<DN,Collection<SearchResult>>();
            final Map<DN,Collection<SearchResult>> matches = new HashMap<DN,Collection<SearchResult>>();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Searching "+ repository.getName()+"...", IProgressMonitor.UNKNOWN);

                        monitor.subTask("Searching "+ repository.getName()+"...");

                        SearchResponse response = new SearchResponse();

                        localPartitionClient.search(request, response);

                        while (response.hasNext()) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            SearchResult result = response.next();

                            DN dn = result.getDn();

                            monitor.subTask("Processing "+dn+"...");

                            results.put(dn, result);

                            Collection<SearchResult> list = search(result);
                            if (list != null && !list.isEmpty()) {
                                links.put(dn, list);

                            } else {
                                list = searchLinks(result);
                                matches.put(dn, list);
                            }

                            monitor.worked(1);
                        }

                    } catch (InterruptedException e) {
                        // ignore

                    } catch (Exception e) {
                        throw new InvocationTargetException(e);

                    } finally {
                        monitor.done();
                    }
                }
            });

            for (DN dn : results.keySet()) {

                SearchResult result = results.get(dn);

                TableItem item = new TableItem(localTable, SWT.NONE);
                item.setText(0, dn.toString());

                item.setData("local", result);

                Collection<SearchResult> list = links.get(dn);
                if (list != null && !list.isEmpty()) {
                    item.setData("links", list);

                } else {
                    list = matches.get(dn);
                    item.setData("matches", list);
                }

                updateStatus(item);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void updateStatus(TableItem item) throws Exception {

        Collection<SearchResult> links = (Collection<SearchResult>)item.getData("links");

        if (links != null && !links.isEmpty()) {

            if (links.size() == 1) {
                item.setText(1, "Linked");
                item.setForeground(1, green);

            } else {
                item.setText(1, links.size()+" Links");
                item.setForeground(1, red);
            }

            return;
        }

        Collection<SearchResult> matches = (Collection<SearchResult>)item.getData("matches");

        if (matches != null && !matches.isEmpty()) {

            if (matches.size() == 1) {
                item.setText(1, "Found Match");

            } else {
                item.setText(1, matches.size()+" Matches");
            }

            item.setForeground(1, blue);
            return;
        }

        item.setText(1, "No Match");
        item.setForeground(1, red);
    }

    public void updateGlobal(TableItem item) throws Exception {

        Collection<SearchResult> links = (Collection<SearchResult>)item.getData("links");

        if (links != null && !links.isEmpty()) {
            for (SearchResult link : links) {
                TableItem ti = new TableItem(globalTable, SWT.NONE);
                ti.setText(0, link.getDn().toString());
                ti.setData(link);
            }

            if (links.size() == 1) {
                SearchResult result = links.iterator().next();
                updateAttributes(globalAttributeTable, globalPartitionClient, result.getDn());
            }

            return;
        }

        Collection<SearchResult> matches = (Collection<SearchResult>)item.getData("matches");

        if (matches != null && !matches.isEmpty()) {
            for (SearchResult link : matches) {
                TableItem ti = new TableItem(globalTable, SWT.NONE);
                ti.setText(0, link.getDn().toString());
                ti.setData(link);
            }

            if (matches.size() == 1) {
                SearchResult result = matches.iterator().next();
                updateAttributes(globalAttributeTable, globalPartitionClient, result.getDn());
            }

            return;
        }
    }

    public void updateAttributes(Table table, PartitionClient partitionClient, DN dn) throws Exception {
        SearchResult result = partitionClient.find(dn);
        Attributes attributes = result.getAttributes();

        for (Attribute attribute : attributes.getAll()) {
            String attributeName = attribute.getName();

            for (Object value : attribute.getValues()) {

                log.debug("Attribute "+attributeName+": "+value.getClass().getSimpleName());

                TableItem attrItem = new TableItem(table, SWT.NONE);
                attrItem.setText(0, attributeName);

                String s;
                if ("objectGUID".equalsIgnoreCase(attributeName)) {
                    s = ActiveDirectoryUtil.getGUID((byte[])value);

                } else if ("seeAlsoObjectGUID".equalsIgnoreCase(attributeName)) {
                    if (value instanceof String) {
                        s = ActiveDirectoryUtil.getGUID(((String)value).getBytes());

                    } else {
                        s = ActiveDirectoryUtil.getGUID((byte[])value);
                    }

                } else if ("objectSid".equalsIgnoreCase(attributeName)) {
                    s = ActiveDirectoryUtil.getSID((byte[])value);

                } else if (value instanceof byte[]) {
                    s = BinaryUtil.encode(BinaryUtil.BIG_INTEGER, (byte[])value);

                } else {
                    s = value.toString();
                }

                attrItem.setText(1, s);
            }
        }
    }

    public Collection<SearchResult> search(SearchResult localEntry) {
        try {
            Repository repository = editor.getRepository();
            String localAttribute = repository.getParameter("localAttribute");
            String globalAttribute = repository.getParameter("globalAttribute");

            SimpleFilter filter;
            
            if (localAttribute == null || globalAttribute == null) {
                DN localDn = localEntry.getDn();
                filter = new SimpleFilter("seeAlso", "=", localDn.toString());

            } else {
                Object localValue = localEntry.getAttributes().getValue(localAttribute);
                filter = new SimpleFilter(globalAttribute, "=", localValue);
            }

            SearchRequest request = new SearchRequest();
            request.setDn(globalBaseDn);
            request.setFilter(filter);

            SearchResponse response = new SearchResponse();

            globalPartitionClient.search(request, response);

            return response.getAll();

        } catch (Exception e) {
            return null;
        }
    }

    public Collection<SearchResult> searchLinks(SearchResult result) {
        try {
            Attributes attributes = result.getAttributes();
            String uid = (String)attributes.getValue("uid");
            String cn = (String)attributes.getValue("cn");

            Filter filter = null;
            filter = FilterTool.appendOrFilter(filter, createFilter("uid", uid));
            filter = FilterTool.appendOrFilter(filter, createFilter("cn", cn));

            if (filter == null) return null;

            SearchRequest request = new SearchRequest();
            request.setDn(globalBaseDn);
            request.setFilter(filter);

            SearchResponse response = new SearchResponse();

            globalPartitionClient.search(request, response);

            return response.getAll();

        } catch (Exception e) {
            return null;
        }
    }

    public void createLink(SearchResult globalEntry, SearchResult localEntry) throws Exception {

        Repository repository = editor.getRepository();
        String localAttribute = repository.getParameter("localAttribute");
        String globalAttribute = repository.getParameter("globalAttribute");

        Attributes globalAttributes = globalEntry.getAttributes();
        Attribute objectClass = globalAttributes.get("objectClass");

        if (!objectClass.containsValue("extensibleObject")) {
            Collection<Modification> modifications = new ArrayList<Modification>();
            modifications.add(new Modification(Modification.ADD, new Attribute("objectClass", "extensibleObject")));

            globalPartitionClient.modify(globalEntry.getDn(), modifications);
        }

        Collection<Modification> modifications = new ArrayList<Modification>();

        if (localAttribute == null || globalAttribute == null) {
            String localDn = localEntry.getDn().toString();
            modifications.add(new Modification(Modification.ADD, new Attribute("seeAlso", localDn)));

        } else {
            Object localValue = localEntry.getAttributes().getValue(localAttribute);
            modifications.add(new Modification(Modification.ADD, new Attribute(globalAttribute, localValue)));
        }

        globalPartitionClient.modify(globalEntry.getDn(), modifications);
    }

    public void removeLink(DN globalDn, SearchResult localEntry) throws Exception {

        Repository repository = editor.getRepository();
        String localAttribute = repository.getParameter("localAttribute");
        String globalAttribute = repository.getParameter("globalAttribute");

        Collection<Modification> modifications = new ArrayList<Modification>();

        if (localAttribute == null || globalAttribute == null) {
            String localDn = localEntry.getDn().toString();
            modifications.add(new Modification(Modification.DELETE, new Attribute("seeAlso", localDn)));

        } else {
            Object localValue = localEntry.getAttributes().getValue(localAttribute);
            modifications.add(new Modification(Modification.DELETE, new Attribute(globalAttribute, localValue)));
        }

        globalPartitionClient.modify(globalDn, modifications);
    }

    public void link() {
        try {
            int count = localTable.getSelectionCount();
            if (count == 0) return;

            for (TableItem item : localTable.getSelection()) {

                Collection<SearchResult> links = (Collection<SearchResult>)item.getData("links");
                if (links != null && !links.isEmpty()) continue;

                Collection<SearchResult> matches = (Collection<SearchResult>)item.getData("matches");
                if (matches == null || matches.isEmpty()) continue;

                SearchResult result = (SearchResult)item.getData("local");

                if (matches.size() == 1) {
                    SearchResult globalResult = matches.iterator().next();
                    createLink(globalResult, result);

                    links = new ArrayList<SearchResult>();
                    links.add(globalResult);
                    item.setData("links", links);
                    item.setData("matches", null);

                    updateStatus(item);

                } else if (globalTable.getSelectionCount() == 1) {

                    TableItem globalItem = globalTable.getSelection()[0];

                    SearchResult globalResult = (SearchResult)globalItem.getData();
                    createLink(globalResult, result);

                    links = new ArrayList<SearchResult>();
                    links.add(globalResult);
                    item.setData("links", links);
                    item.setData("matches", null);

                    updateStatus(item);
                }
            }

            globalTable.removeAll();
            globalAttributeTable.removeAll();

            if (count == 1) {
                TableItem item = localTable.getSelection()[0];
                updateGlobal(item);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void unlink() {
        try {
            int count = localTable.getSelectionCount();
            if (count == 0) return;

            boolean confirm = MessageDialog.openQuestion(
                    editor.getSite().getShell(),
                    "Unlink",
                    "Are you sure?"
            );

            if (!confirm) return;

            for (TableItem item : localTable.getSelection()) {
                SearchResult result = (SearchResult)item.getData("local");

                Collection<SearchResult> links = (Collection<SearchResult>)item.getData("links");
                if (links == null || links.isEmpty()) continue;

                if (globalTable.getSelectionCount() == 0) {

                    for (SearchResult link : links) {
                        removeLink(link.getDn(), result);
                    }

                    links = null;
                    item.setData("links", null);

                } else {
                    for (TableItem globalItem : globalTable.getSelection()) {
                        SearchResult link = (SearchResult)globalItem.getData();
                        removeLink(link.getDn(), result);
                        links.remove(link);
                    }

                    if (links.isEmpty()) {
                        links = null;
                        item.setData("links", null);
                    }
                }

                if (links == null) {
                    Collection<SearchResult> matches = searchLinks(result);
                    item.setData("matches", matches);
                }

                updateStatus(item);
            }

            globalTable.removeAll();
            globalAttributeTable.removeAll();

            if (count == 1) {
                TableItem item = localTable.getSelection()[0];
                updateGlobal(item);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void search() {
        try {
            if (localTable.getSelectionCount() != 1) return;

            TableItem item = localTable.getSelection()[0];

            SearchResult result = (SearchResult)item.getData("local");
            DN dn = result.getDn();

            LinkingWizard wizard = new LinkingWizard();
            wizard.setDn(globalBaseDn);
            wizard.setSearchResult(result);
            wizard.setPartitionClient(globalPartitionClient);

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
            dialog.setPageSize(600, 500);

            if (dialog.open() != Window.OK) return;

            Collection<SearchResult> results = wizard.getResults();
            for (SearchResult globalResult : results) {
                createLink(globalResult, result);
            }

            Collection<SearchResult> links = (Collection<SearchResult>)item.getData("links");
            if (links == null) {
                links = new ArrayList<SearchResult>();
                item.setData("links", links);
            }
            links.addAll(results);

            updateStatus(item);

            globalTable.removeAll();
            globalAttributeTable.removeAll();

            updateGlobal(item);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void create() {
        try {
            int count = localTable.getSelectionCount();
            if (count == 0) return;

            for (TableItem item : localTable.getSelection()) {
                SearchResult result = (SearchResult)item.getData("local");

                SearchResult globalResult = createEntry(result);

                Collection<SearchResult> links = (Collection<SearchResult>)item.getData("links");
                if (links == null) {
                    links = new ArrayList<SearchResult>();
                    item.setData("links", links);
                }
                links.add(globalResult);

                updateStatus(item);
            }

            globalTable.removeAll();
            globalAttributeTable.removeAll();

            if (count == 1) {
                TableItem item = localTable.getSelection()[0];
                updateGlobal(item);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public SearchResult createEntry(SearchResult localEntry) throws Exception {

        Repository repository = editor.getRepository();
        String localAttribute = repository.getParameter("localAttribute");
        String globalAttribute = repository.getParameter("globalAttribute");

        SearchResult globalResult = (SearchResult)localEntry.clone();

        DN dn = globalResult.getDn().getPrefix(localBaseDn).append(globalBaseDn);
        globalResult.setDn(dn);
        
        Attributes attributes = globalResult.getAttributes();
        attributes.addValue("objectClass", "extensibleObject");

        if (localAttribute == null || globalAttribute == null) {
            String localDn = localEntry.getDn().toString();
            attributes.addValue("seeAlso", localDn);

        } else {
            Object localValue = localEntry.getAttributes().getValue(localAttribute);
            attributes.addValue(globalAttribute, localValue);
        }

        globalPartitionClient.add(dn, attributes);

        return globalResult;
    }
}
