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

import java.util.*;
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

    Text localBaseText;
    Text localFilterText;
    Combo localScopeCombo;

    Table localTable;
    Table localAttributeTable;

    Text globalBaseText;
    Text globalFilterText;
    Combo globalScopeCombo;

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
            form.setText("Identity Linking");

            Composite body = form.getBody();
            body.setLayout(new GridLayout(2, false));

            Section localSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            localSection.setText("Local");
            localSection.setLayoutData(new GridData(GridData.FILL_BOTH));

            Control localControl = createLocalControl(localSection);
            localSection.setClient(localControl);

            Section globalSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            globalSection.setText("Global");
            globalSection.setLayoutData(new GridData(GridData.FILL_BOTH));

            Control globalControl = createGlobalControl(globalSection);
            globalSection.setClient(globalControl);
/*
            Section actionsSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            actionsSection.setText("Actions");
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 2;
            actionsSection.setLayoutData(gd);

            Control actionsControl = createActionsControl(actionsSection);
            actionsSection.setClient(actionsControl);
*/
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public Composite createLocalControl(Composite parent) throws Exception {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Composite topPanel = createTopLocalSection(composite);
        topPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite bottomPanel = createBottomLocalSection(composite);
        bottomPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        return composite;
    }

    public Composite createTopLocalSection(Composite parent) throws Exception {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Label baseLabel = toolkit.createLabel(composite, "Base:");
        GridData gd = new GridData();
        gd.widthHint = 50;
        baseLabel.setLayoutData(gd);

        localBaseText = toolkit.createText(composite, localBaseDn.toString(), SWT.BORDER);
        localBaseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button browseButton = toolkit.createButton(composite, "Browse", SWT.PUSH);
        gd = new GridData();
        gd.widthHint = 80;
        browseButton.setLayoutData(gd);

        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    BrowserWizard wizard = new BrowserWizard();
                    wizard.setBaseDn(localBaseDn);
                    wizard.setDn(localBaseText.getText());
                    wizard.setPartitionClient(localPartitionClient);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    if (dialog.open() != Window.OK) return;

                    localBaseText.setText(wizard.getDn());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Label filterLabel = toolkit.createLabel(composite, "Filter:");
        filterLabel.setLayoutData(new GridData());

        String filter = "(objectClass=*)";

        localFilterText = toolkit.createText(composite, filter, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        localFilterText.setLayoutData(gd);

        Label scopeLabel = toolkit.createLabel(composite, "Scope:");
        scopeLabel.setLayoutData(new GridData());

        String scope = "SUBTREE";

        localScopeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        localScopeCombo.add("OBJECT");
        localScopeCombo.add("ONELEVEL");
        localScopeCombo.add("SUBTREE");
        localScopeCombo.setText(scope);

        localScopeCombo.setLayoutData(new GridData());

        Button searchButton = toolkit.createButton(composite, "Search", SWT.PUSH);
        gd = new GridData();
        gd.widthHint = 80;
        searchButton.setLayoutData(gd);

        searchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                searchLocal();
            }
        });

        return composite;
    }

    public Composite createBottomLocalSection(Composite parent) throws Exception {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        localTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_BOTH);
        //gd.heightHint = 150;
        localTable.setLayoutData(gd);

        localTable.setHeaderVisible(true);
        localTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(localTable, SWT.NONE);
        tc.setText("DN");
        tc.setWidth(230);

        tc = new TableColumn(localTable, SWT.NONE);
        tc.setText("Link");
        tc.setWidth(70);

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

        Menu menu = new Menu(localTable);
        localTable.setMenu(menu);

        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Link");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                linkEntries();
            }
        });

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Unlink");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                unlinkEntries();
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Import");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                importEntries();
            }
        });

        localAttributeTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 200;
        localAttributeTable.setLayoutData(gd);

        localAttributeTable.setHeaderVisible(true);
        localAttributeTable.setLinesVisible(true);

        tc = new TableColumn(localAttributeTable, SWT.NONE);
        tc.setText("Attribute");
        tc.setWidth(100);

        tc = new TableColumn(localAttributeTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(200);

        return composite;
    }

    public Composite createGlobalControl(Composite parent) throws Exception {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Composite topPanel = createTopGlobalSection(composite);
        topPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite bottomPanel = createBottomGlobalSection(composite);
        bottomPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        return composite;
    }

    public Composite createTopGlobalSection(Composite parent) throws Exception {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Label baseLabel = toolkit.createLabel(composite, "Base:");
        GridData gd = new GridData();
        gd.widthHint = 50;
        baseLabel.setLayoutData(gd);

        globalBaseText = toolkit.createText(composite, globalBaseDn.toString(), SWT.BORDER);
        globalBaseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button browseButton = toolkit.createButton(composite, "Browse", SWT.PUSH);
        gd = new GridData();
        gd.widthHint = 80;
        browseButton.setLayoutData(gd);

        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    BrowserWizard wizard = new BrowserWizard();
                    wizard.setBaseDn(globalBaseDn);
                    wizard.setDn(globalBaseText.getText());
                    wizard.setPartitionClient(globalPartitionClient);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    if (dialog.open() != Window.OK) return;

                    globalBaseText.setText(wizard.getDn());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Label filterLabel = toolkit.createLabel(composite, "Filter:");
        filterLabel.setLayoutData(new GridData());

        String filter = "(objectClass=*)";

        globalFilterText = toolkit.createText(composite, filter, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        globalFilterText.setLayoutData(gd);

        Label scopeLabel = toolkit.createLabel(composite, "Scope:");
        scopeLabel.setLayoutData(new GridData());

        String scope = "SUBTREE";

        globalScopeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        globalScopeCombo.add("OBJECT");
        globalScopeCombo.add("ONELEVEL");
        globalScopeCombo.add("SUBTREE");
        globalScopeCombo.setText(scope);

        globalScopeCombo.setLayoutData(new GridData());

        Button searchButton = toolkit.createButton(composite, "Search", SWT.PUSH);
        gd = new GridData();
        gd.widthHint = 80;
        searchButton.setLayoutData(gd);

        searchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                searchGlobal();
            }
        });

        return composite;
    }

    public Composite createBottomGlobalSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
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

        Menu menu = new Menu(globalTable);
        globalTable.setMenu(menu);

        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Link");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                linkEntries();
            }
        });

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Unlink");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                unlinkEntries();
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Delete");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                deleteEntries();
            }
        });

        globalAttributeTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 200;
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

    public Composite createActionsControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        RowLayout layout = new RowLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
/*
        toolkit.createLabel(composite, "Global Filter:");

        globalFilterText = toolkit.createText(composite, "", SWT.BORDER);
        RowData rd = new RowData();
        rd.width = 200;
        globalFilterText.setLayoutData(rd);

        Button matchButton = toolkit.createButton(composite, "  Search Global  ", SWT.PUSH);

        matchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                searchGlobal();
            }
        });

        toolkit.createLabel(composite, "  ", SWT.NONE);
*/
        Button linkButton = toolkit.createButton(composite, "  Link  ", SWT.PUSH);

        linkButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                linkEntries();
            }
        });

        Button unlinkButton = toolkit.createButton(composite, "  Unlink  ", SWT.PUSH);

        unlinkButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                unlinkEntries();
            }
        });

        toolkit.createLabel(composite, "  ", SWT.NONE);

        Button importButton = toolkit.createButton(composite, "  Import  ", SWT.PUSH);

        importButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                importEntries();
            }
        });
/*
        toolkit.createLabel(composite, "  ", SWT.NONE);

        Button manualButton = toolkit.createButton(composite, "  Manual  ", SWT.PUSH);

        manualButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                manualSearch();
            }
        });
*/
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

    public void searchLocal() {
        try {
            localTable.removeAll();
            localAttributeTable.removeAll();
            globalTable.removeAll();
            globalAttributeTable.removeAll();

            final SearchRequest request = new SearchRequest();
            request.setDn(localBaseText.getText());
            request.setFilter(localFilterText.getText());
            request.setScope(localScopeCombo.getSelectionIndex());

            final SearchResponse response = new SearchResponse();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Searching "+ repository.getName()+"...", IProgressMonitor.UNKNOWN);

                        localPartitionClient.search(request, response);

                    } catch (InterruptedException e) {
                        // ignore

                    } catch (Exception e) {
                        throw new InvocationTargetException(e);

                    } finally {
                        monitor.done();
                    }
                }
            });

            while (response.hasNext()) {
                SearchResult result = response.next();
                DN dn = result.getDn();

                TableItem item = new TableItem(localTable, SWT.NONE);
                item.setText(0, dn.toString());
                item.setData("local", result);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void searchGlobal() {
        try {
            globalTable.removeAll();
            globalAttributeTable.removeAll();

            TableItem items[] = localTable.getSelection();
            if (items.length == 0) items = localTable.getItems();

            final Collection<SearchResult> results = new ArrayList<SearchResult>();

            for (TableItem item : items) {
                SearchResult result = (SearchResult)item.getData("local");
                results.add(result);
            }

            final String baseDn = globalBaseText.getText();
            final String filter = globalFilterText.getText();
            final int scope = globalScopeCombo.getSelectionIndex();

            final Map<DN,Collection<SearchResult>> links = new LinkedHashMap<DN,Collection<SearchResult>>();
            final Map<DN,Collection<SearchResult>> matches = new LinkedHashMap<DN,Collection<SearchResult>>();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Searching "+ repository.getName()+"...", results.size());

                        for (SearchResult result : results) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            DN dn = result.getDn();

                            monitor.subTask("Processing "+dn+"...");

                            Collection<SearchResult> list = searchLinks(result);
                            if (list != null && !list.isEmpty()) {
                                links.put(dn, list);

                            } else {
                                Filter f = createFilter(result, filter);
                                list = searchLinks(baseDn, f, scope);
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

            for (TableItem item : items) {
                SearchResult result = (SearchResult)item.getData("local");
                DN dn = result.getDn();

                Collection<SearchResult> list = links.get(dn);
                if (list != null && !list.isEmpty()) {
                    item.setData("links", list);

                } else {
                    list = matches.get(dn);
                    item.setData("matches", list);
                }

                updateStatus(item);
            }

            if (items.length != 1) return;

            TableItem item = items[0];
            updateGlobal(item);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void manualSearch() {
        try {
            if (localTable.getSelectionCount() != 1) return;

            TableItem item = localTable.getSelection()[0];

            SearchResult result = (SearchResult)item.getData("local");

            String matchFilter = globalFilterText.getText();
            Filter filter = createFilter(result, matchFilter);

            LinkingWizard wizard = new LinkingWizard();
            wizard.setDn(globalBaseDn);
            wizard.setFilter(filter);
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
                item.setText(1, "1 Match");

            } else {
                item.setText(1, matches.size()+" Matches");
            }

            item.setForeground(1, blue);
            return;
        }

        item.setText(1, "Not Found");
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

        log.debug("Attributes:");

        for (Attribute attribute : attributes.getAll()) {
            String attributeName = attribute.getName();

            log.debug(attribute);

            for (Object value : attribute.getValues()) {

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

    public Collection<SearchResult> searchLinks(SearchResult localEntry) {
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

    public Filter createFilter(SearchResult result, String linkFilter) throws Exception {
        if ("".equals(linkFilter)) return null;

        log.debug("Searching links for "+result.getDn());
        Attributes attributes = result.getAttributes();

        StringBuilder sb = new StringBuilder(linkFilter);
        int start = 0;

        while (start < sb.length()) {
            int i = sb.indexOf("${", start);
            if (i < 0) break;

            int j = sb.indexOf("}", i+2);
            if (j < 0) break;

            String name = sb.substring(i+2, j);
            Object value = attributes.getValue(name);
            String s = value == null ? "" : value.toString();

            sb.replace(i, j+1, s);
            start = i+s.length();
        }

        String s = sb.toString();
        log.debug("Link filter: "+s);

        return FilterTool.parseFilter(s);
    }

    public Collection<SearchResult> searchLinks(String baseDn, Filter filter, int scope) throws Exception {
        
        if (filter == null) return null;

        //try {
            SearchRequest request = new SearchRequest();
            request.setDn(baseDn);
            request.setFilter(filter);
            request.setScope(scope);

            SearchResponse response = new SearchResponse();

            globalPartitionClient.search(request, response);

            return response.getAll();

        //} catch (Exception e) {
        //    return null;
        //}
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

    public void linkEntries() {
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

    public void unlinkEntries() {
        try {
            int count = localTable.getSelectionCount();
            if (count == 0) return;

            boolean confirm = MessageDialog.openQuestion(
                    editor.getSite().getShell(),
                    "Unlink",
                    "Are you sure?"
            );

            if (!confirm) return;

            String baseDn = globalBaseText.getText();
            String filter = globalFilterText.getText();
            int scope = globalScopeCombo.getSelectionIndex();

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
                    Filter f = createFilter(result, filter);
                    Collection<SearchResult> matches = searchLinks(baseDn, f, scope);
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

    public void importEntries() {
        try {
            int count = localTable.getSelectionCount();
            if (count == 0) return;

            final Collection<SearchResult> results = new ArrayList<SearchResult>();

            for (TableItem item : localTable.getSelection()) {

                Collection<SearchResult> list = (Collection<SearchResult>)item.getData("links");
                if (list != null && !list.isEmpty()) continue;

                SearchResult result = (SearchResult)item.getData("local");
                results.add(result);

                updateStatus(item);
            }

            final Map<DN,SearchResult> links = new LinkedHashMap<DN,SearchResult>();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Importing "+repository.getName()+"...", results.size());

                        for (SearchResult result : results) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            DN dn = result.getDn();

                            monitor.subTask("Processing "+dn+"...");

                            SearchResult globalResult = createEntry(result);
                            links.put(dn, globalResult);

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

            for (TableItem item : localTable.getSelection()) {

                Collection<SearchResult> list = (Collection<SearchResult>)item.getData("links");
                if (list != null && !list.isEmpty()) continue;

                SearchResult result = (SearchResult)item.getData("local");
                SearchResult globalResult = links.get(result.getDn());
                if (globalResult == null) continue;

                list = new ArrayList<SearchResult>();
                list.add(globalResult);

                item.setData("links", list);

                item.setText(1, "Linked");
                item.setForeground(1, green);
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

    public void deleteEntries() {
        try {
            final Collection<DN> dns = new ArrayList<DN>();

            for (TableItem item : globalTable.getSelection()) {
                SearchResult result = (SearchResult)item.getData();
                dns.add(result.getDn());
            }

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Deleting "+repository.getName()+"...", dns.size());

                        for (DN dn : dns) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            monitor.subTask("Deleting "+dn+"...");

                            globalPartitionClient.delete(dn);

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

            for (TableItem item : globalTable.getSelection()) {
                item.dispose();
            }

            globalAttributeTable.removeAll();

            TableItem item = localTable.getSelection()[0];

            Collection<SearchResult> list = (Collection<SearchResult>)item.getData("links");

            if (list != null) {
                Collection<SearchResult> newList = new ArrayList<SearchResult>();
                for (SearchResult result : list) {
                    if (dns.contains(result.getDn())) continue;
                    newList.add(result);
                }

                if (newList.isEmpty()) {
                    item.setData("links", null);
                } else {
                    item.setData("links", newList);
                }
            }

            list = (Collection<SearchResult>)item.getData("matches");

            if (list != null) {
                Collection<SearchResult> newList = new ArrayList<SearchResult>();
                for (SearchResult result : list) {
                    if (dns.contains(result.getDn())) continue;
                    newList.add(result);
                }

                if (newList.isEmpty()) {
                    item.setData("matches", null);
                } else {
                    item.setData("matches", newList);
                }
            }

            updateStatus(item);
            
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
