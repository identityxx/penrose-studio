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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.federation.wizard.BrowserWizard;
import org.safehaus.penrose.studio.federation.Repository;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.SubstringFilter;
import org.safehaus.penrose.filter.FilterTool;
import org.safehaus.penrose.filter.SimpleFilter;
import org.safehaus.penrose.util.ActiveDirectoryUtil;
import org.safehaus.penrose.util.BinaryUtil;

import java.util.Collection;
import java.util.ArrayList;

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

    private Partition partition;

    Source localSource;
    Source globalSource;

    DN localBaseDn;
    DN globalBaseDn;

    public LinkingPage(LinkingEditor editor) throws Exception {
        super(editor, "IDENTITY_LINKING", "  Identity Linking  ");

        this.editor = editor;
        this.partition = editor.getPartition();

        localSource = partition.getSource("Local");
        globalSource = partition.getSource("Global");

        localBaseDn = new DN(localSource.getParameter("baseDn"));
        globalBaseDn = new DN(globalSource.getParameter("baseDn"));

        Display display = Display.getDefault();

        red = display.getSystemColor(SWT.COLOR_RED);
        green = display.getSystemColor(SWT.COLOR_GREEN);
        blue = display.getSystemColor(SWT.COLOR_BLUE);
    }

    public void createFormContent(IManagedForm managedForm) {
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
    }

    public Composite createLocalControl(Composite parent) {

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

    public Composite createTopLocalSection(Composite parent) {

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
                    wizard.setBaseDn(localBaseDn.toString());
                    wizard.setDn(baseDnText.getText());
                    wizard.setSource(localSource);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    if (dialog.open() != Window.OK) return;

                    baseDnText.setText(wizard.getDn());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        Label filterLabel = toolkit.createLabel(composite, "Filter:");
        gd = new GridData();
        gd.widthHint = 100;
        filterLabel.setLayoutData(gd);

        String filter = localSource.getParameter("filter");
        if (filter == null) filter = "(objectClass=*)";

        filterText = toolkit.createText(composite, filter, SWT.BORDER);
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(composite, SWT.NONE);
        
        Label scopeLabel = toolkit.createLabel(composite, "Scope:");
        gd = new GridData();
        gd.widthHint = 100;
        scopeLabel.setLayoutData(gd);

        String scope = localSource.getParameter("scope");
        if (scope == null) scope = "SUBTREE";

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
                    updateAttributes(localAttributeTable, localSource, local.getDn());

                    updateGlobal(item);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
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
                IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
/*
                try {
                    progressService.run(true, true, new IRunnableWithProgress() {
                        public void run(final IProgressMonitor monitor) {
                            Display.getDefault().asyncExec(new Runnable() {
                                public void run() {
                                    search(monitor);
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
*/
                Job job = new Job("Search") {
                    public IStatus run(final IProgressMonitor monitor) {
                        Display.getDefault().asyncExec(new Runnable() {
                            public void run() {
                                search(monitor);
                            }
                        });
                        return Status.OK_STATUS;
                    }
                };

                Shell shell = new Shell();
                shell.setSize(200, 100);

                progressService.showInDialog(shell, job);

                job.schedule();
            }
        });

        toolkit.createLabel(buttons, "  ", SWT.NONE);

        Button linkButton = toolkit.createButton(buttons, "  Link  ", SWT.PUSH);

        linkButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
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
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        Button unlinkButton = toolkit.createButton(buttons, "  Unlink  ", SWT.PUSH);

        unlinkButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
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
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        toolkit.createLabel(buttons, "  ", SWT.NONE);

        Button searchButton = toolkit.createButton(buttons, "  Search  ", SWT.PUSH);

        searchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (localTable.getSelectionCount() != 1) return;

                    TableItem item = localTable.getSelection()[0];

                    SearchResult result = (SearchResult)item.getData("local");
                    DN dn = result.getDn().append(localBaseDn);

                    LinkingWizard wizard = new LinkingWizard();
                    wizard.setDn(dn);
                    wizard.setSearchResult(result);
                    wizard.setSource(globalSource);
                    wizard.setBaseDn(globalBaseDn);

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
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        Button createButton = toolkit.createButton(buttons, "  Create  ", SWT.PUSH);

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
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
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
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
                    updateAttributes(globalAttributeTable, globalSource, link.getDn());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
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

    public void search(final IProgressMonitor monitor) {
        try {
            monitor.beginTask("Searching "+partition.getName()+"...", IProgressMonitor.UNKNOWN);

            localTable.removeAll();
            localAttributeTable.removeAll();
            globalTable.removeAll();
            globalAttributeTable.removeAll();

            DN searchBaseDn = new DN(baseDnText.getText());
            searchBaseDn = searchBaseDn.getPrefix(localBaseDn);

            String filter = filterText.getText();
            if ("".equals(filter)) filter = "(objectClass=*)";

            int scope = scopeCombo.getSelectionIndex();

            log.debug("Searching ["+searchBaseDn+"]");

            SearchRequest request = new SearchRequest();
            request.setDn(searchBaseDn);
            request.setFilter(filter);
            request.setScope(scope);

            SearchResponse response = new SearchResponse() {
                public void add(SearchResult result) throws Exception {

                    if (monitor.isCanceled()) {
                        close();
                        return;
                    }

                    String dn = result.getDn().append(localBaseDn).toString();

                    TableItem item = new TableItem(localTable, SWT.NONE);
                    item.setText(0, dn);

                    item.setData("local", result);

                    Collection<SearchResult> links = getLinks(result);

                    if (links == null || links.isEmpty()) {
                        Collection<SearchResult> matches = searchLinks(result);
                        item.setData("matches", matches);

                    } else {
                        item.setData("links", links);
                    }

                    updateStatus(item);

                    monitor.worked((int)getTotalCount());
                }
            };

            localSource.search(request, response);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());

        } finally {
            monitor.done();
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
                ti.setText(0, link.getDn().append(globalBaseDn).toString());
                ti.setData(link);
            }

            if (links.size() == 1) {
                SearchResult result = links.iterator().next();
                updateAttributes(globalAttributeTable, globalSource, result.getDn());
            }

            return;
        }

        Collection<SearchResult> matches = (Collection<SearchResult>)item.getData("matches");

        if (matches != null && !matches.isEmpty()) {
            for (SearchResult link : matches) {
                TableItem ti = new TableItem(globalTable, SWT.NONE);
                ti.setText(0, link.getDn().append(globalBaseDn).toString());
                ti.setData(link);
            }

            if (matches.size() == 1) {
                SearchResult result = matches.iterator().next();
                updateAttributes(globalAttributeTable, globalSource, result.getDn());
            }

            return;
        }
    }

    public void updateAttributes(Table table, Source source, DN dn) throws Exception {
        SearchResult result = source.find(dn);
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

    public Collection<SearchResult> getLinks(SearchResult localEntry) {
        try {
            Repository repository = editor.getRepository();
            String localAttribute = repository.getParameter("localAttribute");
            String globalAttribute = repository.getParameter("globalAttribute");

            SimpleFilter filter;
            
            if (localAttribute == null || globalAttribute == null) {
                DN localDn = localEntry.getDn().append(localBaseDn);
                filter = new SimpleFilter("seeAlso", "=", localDn.toString());

            } else {
                Object localValue = localEntry.getAttributes().getValue(localAttribute);
                filter = new SimpleFilter(globalAttribute, "=", localValue);
            }

            SearchResponse response = globalSource.search((DN)null, filter, SearchRequest.SCOPE_SUB);
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
            request.setFilter(filter);

            SearchResponse response = new SearchResponse();

            globalSource.search(request, response);

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

            globalSource.modify(globalEntry.getDn(), modifications);
        }

        Collection<Modification> modifications = new ArrayList<Modification>();

        if (localAttribute == null || globalAttribute == null) {
            String localDn = localEntry.getDn().append(localBaseDn).toString();
            modifications.add(new Modification(Modification.ADD, new Attribute("seeAlso", localDn)));

        } else {
            Object localValue = localEntry.getAttributes().getValue(localAttribute);
            modifications.add(new Modification(Modification.ADD, new Attribute(globalAttribute, localValue)));
        }

        globalSource.modify(globalEntry.getDn(), modifications);
    }

    public void removeLink(DN globalDn, SearchResult localEntry) throws Exception {

        Repository repository = editor.getRepository();
        String localAttribute = repository.getParameter("localAttribute");
        String globalAttribute = repository.getParameter("globalAttribute");

        Collection<Modification> modifications = new ArrayList<Modification>();

        if (localAttribute == null || globalAttribute == null) {
            String localDn = localEntry.getDn().append(localBaseDn).toString();
            modifications.add(new Modification(Modification.DELETE, new Attribute("seeAlso", localDn)));

        } else {
            Object localValue = localEntry.getAttributes().getValue(localAttribute);
            modifications.add(new Modification(Modification.DELETE, new Attribute(globalAttribute, localValue)));
        }

        globalSource.modify(globalDn, modifications);
    }

    public SearchResult createEntry(SearchResult localEntry) throws Exception {

        Repository repository = editor.getRepository();
        String localAttribute = repository.getParameter("localAttribute");
        String globalAttribute = repository.getParameter("globalAttribute");

        SearchResult globalResult = (SearchResult)localEntry.clone();

        DN dn = globalResult.getDn();
        Attributes attributes = globalResult.getAttributes();
        attributes.addValue("objectClass", "extensibleObject");

        if (localAttribute == null || globalAttribute == null) {
            String localDn = localEntry.getDn().append(localBaseDn).toString();
            attributes.addValue("seeAlso", localDn);

        } else {
            Object localValue = localEntry.getAttributes().getValue(localAttribute);
            attributes.addValue(globalAttribute, localValue);
        }

        globalSource.add(dn, attributes);

        return globalResult;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
