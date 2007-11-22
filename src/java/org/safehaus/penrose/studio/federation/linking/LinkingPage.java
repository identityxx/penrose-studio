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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TableViewer;
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

    TableViewer localTableViewer;
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

        localTableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        localTableViewer.setContentProvider(new LocalTableContentProvider());
        localTableViewer.setLabelProvider(new LocalTableLabelProvider());

        localTable = localTableViewer.getTable();

        //localTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);

        GridData gd = new GridData(GridData.FILL_BOTH);
        //gd.heightHint = 150;
        localTable.setLayoutData(gd);

        localTable.setHeaderVisible(true);
        localTable.setLinesVisible(true);

        localTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    localAttributeTable.removeAll();
                    globalTable.removeAll();
                    globalAttributeTable.removeAll();

                    if (localTable.getSelectionCount() != 1) return;

                    TableItem item = localTable.getSelection()[0];

                    Data data = (Data)item.getData();
                    updateAttributes(localAttributeTable, localPartitionClient, data.getEntry());

                    updateGlobal(data);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        final TableColumn dnTableColumn = new TableColumn(localTable, SWT.NONE);
        dnTableColumn.setText("DN");
        dnTableColumn.setWidth(230);

        dnTableColumn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                //sortByDn();
                localTable.setSortColumn(dnTableColumn);
                localTable.setSortDirection(SWT.DOWN);
            }
        });

        final TableColumn statusTableColumn = new TableColumn(localTable, SWT.NONE);
        statusTableColumn.setText("Status");
        statusTableColumn.setWidth(70);

        statusTableColumn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                //sortByStatus();
                localTable.setSortColumn(statusTableColumn);
                localTable.setSortDirection(SWT.DOWN);
            }
        });


        Menu menu = new Menu(localTable);
        localTable.setMenu(menu);

        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Link");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                linkLocalEntries();
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

        TableColumn attributeTableColumn = new TableColumn(localAttributeTable, SWT.NONE);
        attributeTableColumn.setText("Attribute");
        attributeTableColumn.setWidth(100);

        TableColumn valueTableColumn = new TableColumn(localAttributeTable, SWT.NONE);
        valueTableColumn.setText("Value");
        valueTableColumn.setWidth(200);

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

                    DN dn = (DN)item.getData("global");
                    updateAttributes(globalAttributeTable, globalPartitionClient, dn);

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
                linkGlobalEntries();
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

            final Collection<Data> results = new ArrayList<Data>();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Searching "+ repository.getName()+"...", IProgressMonitor.UNKNOWN);

                        SearchResponse response = new SearchResponse();

                        localPartitionClient.search(request, response);

                        while (response.hasNext()) {
                            SearchResult result = response.next();

                            Data data = new Data();
                            data.setEntry(result);

                            results.add(data);
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

            localTableViewer.setInput(results);

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

            final Collection<Data> results = new ArrayList<Data>();

            for (TableItem item : items) {
                Data data = (Data)item.getData();
                data.setSearched(false);
                data.removeLinks();
                data.removeMatches();
                results.add(data);
            }

            final String baseDn = globalBaseText.getText();
            final String filter = globalFilterText.getText();
            final int scope = globalScopeCombo.getSelectionIndex();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Searching "+ repository.getName()+"...", results.size());

                        for (Data data : results) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            DN dn = data.getDn();

                            monitor.subTask("Processing "+dn+"...");

                            data.setSearched(true);

                            Collection<DN> list = searchLinks(data.getEntry());
                            if (list != null && !list.isEmpty()) {
                                data.setLinks(list);

                            } else {
                                Filter f = createFilter(data.getEntry(), filter);
                                list = searchLinks(baseDn, f, scope);
                                data.setMatches(list);
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

            localTableViewer.refresh();
            
            if (items.length == 1) {
                TableItem item = items[0];
                Data data = (Data)item.getData();
                updateGlobal(data);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void updateGlobal(Data data) throws Exception {

        Collection<DN> links = data.getLinks();

        if (!links.isEmpty()) {
            for (DN dn : links) {
                TableItem ti = new TableItem(globalTable, SWT.NONE);
                ti.setText(0, dn.toString());
                ti.setData("global", dn);

                if (links.size() == 1) {
                    updateAttributes(globalAttributeTable, globalPartitionClient, dn);
                }
            }

            return;
        }

        Collection<DN> matches = data.getMatches();
        if (matches.isEmpty()) return;

        for (DN dn : matches) {
            TableItem ti = new TableItem(globalTable, SWT.NONE);
            ti.setText(0, dn.toString());
            ti.setData("global", dn);

            if (matches.size() == 1) {
                updateAttributes(globalAttributeTable, globalPartitionClient, dn);
            }
        }
    }

    public void updateAttributes(Table table, PartitionClient partitionClient, DN dn) throws Exception {
        SearchResult entry = partitionClient.find(dn);
        updateAttributes(table, partitionClient, entry);
    }

    public void updateAttributes(Table table, PartitionClient partitionClient, SearchResult entry) throws Exception {

        Attributes attributes = entry.getAttributes();
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

    public Collection<DN> searchLinks(SearchResult localEntry) {
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

            Collection<DN> results = new ArrayList<DN>();

            while (response.hasNext()) {
                SearchResult result = response.next();
                DN dn = result.getDn();
                results.add(dn);
            }

            return results;

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

    public Collection<DN> searchLinks(String baseDn, Filter filter, int scope) throws Exception {
        
        if (filter == null) return null;

        SearchRequest request = new SearchRequest();
        request.setDn(baseDn);
        request.setFilter(filter);
        request.setScope(scope);

        SearchResponse response = new SearchResponse();

        globalPartitionClient.search(request, response);

        Collection<DN> results = new ArrayList<DN>();

        while (response.hasNext()) {
            SearchResult result = response.next();
            DN dn = result.getDn();
            results.add(dn);
        }

        return results;
    }

    public void createLink(DN dn, SearchResult localEntry) throws Exception {

        SearchResult globalEntry = globalPartitionClient.find(dn);

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

    public void linkLocalEntries() {
        try {
            int count = localTable.getSelectionCount();
            if (count == 0) return;

            if (count == 1) {
                TableItem item = localTable.getSelection()[0];
                Data data = (Data)item.getData();

                Collection<DN> links = data.getLinks();

                if (!links.isEmpty()) {
                    ErrorDialog.open("This account has been linked.");
                    return;
                }

                Collection<DN> matches = data.getMatches();

                if (matches.isEmpty()) {
                    ErrorDialog.open("Find a global identity to link to this account.");
                    return;
                }

                if (matches.size() > 1) {
                    ErrorDialog.open(
                            "Cannot link multiple global identities.\n"+
                            "Right click on one of the global identities, then select Link."
                    );
                    return;
                }

                SearchResult result = data.getEntry();

                DN dn = matches.iterator().next();
                createLink(dn, result);

                data.addLink(dn);
                data.removeMatches();

            } else {

                for (TableItem item : localTable.getSelection()) {
                    Data data = (Data)item.getData();

                    Collection<DN> links = data.getLinks();
                    if (!links.isEmpty()) continue;

                    Collection<DN> matches = data.getMatches();
                    if (matches.isEmpty() || matches.size() > 1) continue;

                    SearchResult result = data.getEntry();

                    DN dn = matches.iterator().next();
                    createLink(dn, result);

                    data.addLink(dn);
                    data.removeMatches();
                }
            }

            localTableViewer.refresh();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void linkGlobalEntries() {
        try {
            if (localTable.getSelectionCount() != 1) return;
            if (globalTable.getSelectionCount() != 1) return;

            TableItem localItem = localTable.getSelection()[0];
            TableItem globalItem = globalTable.getSelection()[0];

            Data data = (Data)localItem.getData();
            SearchResult localEntry = data.getEntry();

            DN dn = (DN)globalItem.getData("global");

            createLink(dn, localEntry);

            data.addLink(dn);
            data.removeMatches();

            localTableViewer.refresh();

            globalTable.removeAll();
            globalAttributeTable.removeAll();

            updateGlobal(data);

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
                Data data = (Data)item.getData();
                SearchResult entry = data.getEntry();

                Collection<DN> links = data.getLinks();
                if (links.isEmpty()) continue;

                if (globalTable.getSelectionCount() == 0) {

                    for (DN dn : links) {
                        removeLink(dn, entry);
                    }

                    data.removeLinks();

                } else {
                    for (TableItem globalItem : globalTable.getSelection()) {
                        DN dn = (DN)globalItem.getData("global");
                        removeLink(dn, entry);
                        links.remove(dn);
                    }
                }

                if (links.isEmpty()) {
                    Filter f = createFilter(entry, filter);
                    Collection<DN> matches = searchLinks(baseDn, f, scope);
                    data.setMatches(matches);
                }
            }

            localTableViewer.refresh();

            globalTable.removeAll();
            globalAttributeTable.removeAll();

            if (count == 1) {
                TableItem item = localTable.getSelection()[0];
                Data data = (Data)item.getData();
                updateGlobal(data);
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

            boolean confirm = MessageDialog.openQuestion(
                    editor.getSite().getShell(),
                    "Import",
                    "Are you sure?"
            );

            if (!confirm) return;

            final Collection<Data> results = new ArrayList<Data>();

            for (TableItem item : localTable.getSelection()) {
                Data data = (Data)item.getData();

                Collection<DN> list = data.getLinks();
                if (!list.isEmpty()) continue;

                results.add(data);
            }

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Importing "+repository.getName()+"...", results.size());

                        for (Data data : results) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            DN dn = data.getDn();

                            monitor.subTask("Processing "+dn+"...");

                            SearchResult globalResult = createEntry(data.getEntry());
                            data.addLink(globalResult.getDn());
                            data.removeMatches();

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

            localTableViewer.refresh();

            globalTable.removeAll();
            globalAttributeTable.removeAll();

            if (count == 1) {
                TableItem item = localTable.getSelection()[0];
                Data data = (Data)item.getData();
                updateGlobal(data);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void deleteEntries() {
        try {
            if (localTable.getSelectionCount() == 0) return;
            if (globalTable.getSelectionCount() == 0) return;

            boolean confirm = MessageDialog.openQuestion(
                    editor.getSite().getShell(),
                    "Delete",
                    "Are you sure?"
            );

            if (!confirm) return;

            TableItem localItem = localTable.getSelection()[0];
            final Data data = (Data)localItem.getData();

            final SearchResult entry = data.getEntry();
            final Collection<DN> links = data.getLinks();
            final Collection<DN> matches = data.getMatches();

            final String baseDn = globalBaseText.getText();
            final String filter = globalFilterText.getText();
            final int scope = globalScopeCombo.getSelectionIndex();

            final Collection<DN> dns = new ArrayList<DN>();

            for (TableItem item : globalTable.getSelection()) {
                DN dn = (DN)item.getData("global");
                dns.add(dn);
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

                            links.remove(dn);
                            matches.remove(dn);

                            monitor.worked(1);
                        }

                        if (links.isEmpty()) {
                            Filter f = createFilter(entry, filter);
                            Collection<DN> matches = searchLinks(baseDn, f, scope);
                            data.setMatches(matches);
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

            localTableViewer.refresh();

            for (TableItem item : globalTable.getSelection()) {
                item.dispose();
            }

            globalTable.removeAll();
            globalAttributeTable.removeAll();

            updateGlobal(data);

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

    public void sortByDn() {

        Map<DN,SearchResult> entries = new LinkedHashMap<DN,SearchResult>();
        Map<DN,Collection<DN>> links = new LinkedHashMap<DN,Collection<DN>>();
        Map<DN,Collection<DN>> matches = new LinkedHashMap<DN,Collection<DN>>();

        for (TableItem item : localTable.getItems()) {
            Data data = (Data)item.getData();

            SearchResult entry = data.getEntry();
            entries.put(entry.getDn(), entry);

            Collection<DN> list = data.getLinks();
            links.put(entry.getDn(), list);

            list = data.getMatches();
            matches.put(entry.getDn(), list);
        }

        localTable.removeAll();
        localAttributeTable.removeAll();
        globalTable.removeAll();

    }

    public void sortByStatus() {

    }
}
