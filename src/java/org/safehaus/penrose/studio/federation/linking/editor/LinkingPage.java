package org.safehaus.penrose.studio.federation.linking.editor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.progress.IProgressService;
import org.ietf.ldap.LDAPException;
import org.safehaus.penrose.federation.repository.Repository;
import org.safehaus.penrose.federation.module.LinkingException;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.FilterTool;
import org.safehaus.penrose.filter.SubstringFilter;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.management.module.ModuleClient;
import org.safehaus.penrose.management.partition.PartitionClient;
import org.safehaus.penrose.management.partition.PartitionManagerClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.ldap.dialog.EntryDialog;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.federation.linking.*;
import org.safehaus.penrose.studio.federation.wizard.BrowserWizard;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.util.ActiveDirectoryUtil;
import org.safehaus.penrose.util.BinaryUtil;

import javax.management.MBeanException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Endi S. Dewata
 */
public class LinkingPage extends FormPage {

    public Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

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

    Project project;
    Repository repository;
    String partitionName;

    PartitionClient localPartitionClient;
    PartitionClient globalPartitionClient;

    ModuleClient linkingModuleClient;

    DN localBaseDn;
    DN globalBaseDn;

    DNSorter dnSorter         = new DNSorter();
    StatusSorter statusSorter = new StatusSorter();

    public LinkingPage(LinkingEditor editor) throws Exception {
        super(editor, "LINKING", "  Linking  ");

        LinkingEditorInput ei = (LinkingEditorInput)editor.getEditorInput();

        this.project = ei.getProject();
        this.repository = ei.getRepository();
        this.partitionName = ei.getPartitionName();

        PenroseClient penroseClient = project.getClient();

        PartitionManagerClient partitionManagerClient = penroseClient.getPartitionManagerClient();
        localPartitionClient = partitionManagerClient.getPartitionClient(partitionName);
        globalPartitionClient = partitionManagerClient.getPartitionClient(Federation.GLOBAL);

        localBaseDn = localPartitionClient.getSuffixes().iterator().next();
        globalBaseDn = globalPartitionClient.getSuffixes().iterator().next();

        linkingModuleClient = localPartitionClient.getModuleClient("LinkingModule");
    }

    public void createFormContent(IManagedForm managedForm) {
        try {
            toolkit = managedForm.getToolkit();

            ScrolledForm form = managedForm.getForm();
            form.setText("Linking");

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

        Composite centerPanel = createCenterLocalSection(composite);
        centerPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite bottomPanel = createBottomLocalSection(composite);
        bottomPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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

    public Composite createCenterLocalSection(Composite parent) throws Exception {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        localTableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        localTableViewer.setContentProvider(new LocalTableContentProvider());
        localTableViewer.setLabelProvider(new LocalTableLabelProvider());

        localTable = localTableViewer.getTable();

        GridData gd = new GridData(GridData.FILL_BOTH);
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

                    LocalData data = (LocalData)item.getData();
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

                if (localTable.getSortColumn() == dnTableColumn) {
                    dnSorter.invert();

                } else {
                    localTable.setSortColumn(dnTableColumn);
                    localTableViewer.setSorter(dnSorter);
                }

                localTable.setSortDirection(dnSorter.getDirection());
                localTableViewer.refresh();
            }
        });

        final TableColumn statusTableColumn = new TableColumn(localTable, SWT.NONE);
        statusTableColumn.setText("Status");
        statusTableColumn.setWidth(70);

        statusTableColumn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {

                if (localTable.getSortColumn() == statusTableColumn) {
                    statusSorter.invert();

                } else {
                    localTable.setSortColumn(statusTableColumn);
                    localTableViewer.setSorter(statusSorter);
                }

                localTable.setSortDirection(statusSorter.getDirection());
                localTableViewer.refresh();
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

        return composite;
    }

    public Composite createBottomLocalSection(Composite parent) throws Exception {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        localAttributeTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
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

        Composite centerPanel = createCenterGlobalSection(composite);
        centerPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite bottomPanel = createBottomGlobalSection(composite);
        bottomPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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

    public Composite createCenterGlobalSection(Composite parent) {

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
        tc.setWidth(300);

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

        return composite;
    }

    public Composite createBottomGlobalSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        globalAttributeTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 200;
        globalAttributeTable.setLayoutData(gd);

        globalAttributeTable.setHeaderVisible(true);
        globalAttributeTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(globalAttributeTable, SWT.NONE);
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
            localAttributeTable.removeAll();
            globalTable.removeAll();
            globalAttributeTable.removeAll();

            final SearchRequest request = new SearchRequest();
            request.setDn(localBaseText.getText());
            request.setFilter(localFilterText.getText());
            request.setScope(localScopeCombo.getSelectionIndex());

            final Collection<LocalData> results = new ArrayList<LocalData>();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Searching "+ repository.getName()+"...", IProgressMonitor.UNKNOWN);

                        SearchResponse response = new SearchResponse();

                        localPartitionClient.search(request, response);

                        while (response.hasNext()) {
                            SearchResult result = response.next();

                            LocalData data = new LocalData();
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

            final Collection<LocalData> results = new ArrayList<LocalData>();

            for (TableItem item : items) {
                LocalData data = (LocalData)item.getData();
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

                        for (LocalData data : results) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            DN dn = data.getDn();

                            monitor.subTask("Processing "+dn+"...");

                            data.setSearched(true);

                            Collection<DN> list = searchLinks(data.getEntry().getDn());
                            if (list != null && !list.isEmpty()) {
                                data.setLinks(list);

                            } else {
                                Filter f = createFilter(data.getEntry(), filter);
                                list = searchMatches(baseDn, f, scope);
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
                LocalData data = (LocalData)item.getData();
                updateGlobal(data);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void updateGlobal(LocalData data) throws Exception {

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

            if (log.isDebugEnabled()) attribute.print();

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

            if (log.isDebugEnabled()) {
                if (value instanceof byte[]) {
                    String s = BinaryUtil.encode(BinaryUtil.BIG_INTEGER, (byte[]) value);
                    log.debug(" - " + name + ": " + s);

                } else if (value != null) {
                    String s = BinaryUtil.encode(BinaryUtil.BIG_INTEGER, value.toString().getBytes("UTF-8"));
                    log.debug(" - " + name + ": " + value + " (" + s + ")");
                }
            }

            String s = value == null ? "" : FilterTool.escape(value);

            sb.replace(i, j+1, s);
            start = i+s.length();
        }

        String s = sb.toString();
        log.debug("Link filter: "+s);

        return FilterTool.parseFilter(s);
    }

    public Collection<DN> searchMatches(String baseDn, Filter filter, int scope) throws Exception {
        
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

    public void linkLocalEntries() {
        try {
            int count = localTable.getSelectionCount();
            if (count == 0) return;

            if (count == 1) {
                TableItem item = localTable.getSelection()[0];
                LocalData data = (LocalData)item.getData();

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

                DN globalDn = matches.iterator().next();
                linkEntry(result.getDn(), globalDn);

                data.addLink(globalDn);
                data.removeMatches();

            } else {

                for (TableItem item : localTable.getSelection()) {
                    LocalData data = (LocalData)item.getData();

                    Collection<DN> links = data.getLinks();
                    if (!links.isEmpty()) continue;

                    Collection<DN> matches = data.getMatches();
                    if (matches.isEmpty() || matches.size() > 1) continue;

                    SearchResult result = data.getEntry();

                    DN globalDn = matches.iterator().next();
                    linkEntry(result.getDn(), globalDn);

                    data.addLink(globalDn);
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

            LocalData data = (LocalData)localItem.getData();
            SearchResult localEntry = data.getEntry();

            DN globalDn = (DN)globalItem.getData("global");

            linkEntry(localEntry.getDn(), globalDn);

            data.addLink(globalDn);
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
                    getSite().getShell(),
                    "Unlink",
                    "Are you sure?"
            );

            if (!confirm) return;

            String baseDn = globalBaseText.getText();
            String filter = globalFilterText.getText();
            int scope = globalScopeCombo.getSelectionIndex();

            for (TableItem item : localTable.getSelection()) {
                LocalData data = (LocalData)item.getData();
                SearchResult entry = data.getEntry();

                Collection<DN> links = data.getLinks();
                if (links.isEmpty()) continue;

                if (globalTable.getSelectionCount() == 0) {

                    for (DN globalDn : links) {
                        unlinkEntry(entry.getDn(), globalDn);
                    }

                    data.removeLinks();

                } else {
                    for (TableItem globalItem : globalTable.getSelection()) {
                        DN globalDn = (DN)globalItem.getData("global");
                        unlinkEntry(entry.getDn(), globalDn);
                        links.remove(globalDn);
                    }
                }

                if (links.isEmpty()) {
                    Filter f = createFilter(entry, filter);
                    Collection<DN> matches = searchMatches(baseDn, f, scope);
                    data.setMatches(matches);
                }
            }

            localTableViewer.refresh();

            globalTable.removeAll();
            globalAttributeTable.removeAll();

            if (count == 1) {
                TableItem item = localTable.getSelection()[0];
                LocalData data = (LocalData)item.getData();
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
                    getSite().getShell(),
                    "Import",
                    "Are you sure?"
            );

            if (!confirm) return;

            Map<DN,LocalData> map = new HashMap<DN,LocalData>();
            java.util.List<LocalData> results = new ArrayList<LocalData>();

            for (TableItem item : localTable.getSelection()) {
                LocalData data = (LocalData)item.getData();

                Collection<DN> list = data.getLinks();
                if (!list.isEmpty()) continue;

                map.put(data.getDn(), data);
                results.add(data);
            }

            ImportTask task = new ImportTask(this, repository);
            task.setResults(results);

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            while (true) {
                try {
                    progressService.busyCursorWhile(task);
                    break;

                } catch (InterruptedException e) {
                    break;

                } catch (InvocationTargetException e) {
                    LinkingException le = (LinkingException)e.getCause();

                    ErrorDialog.open(
                            "Failed importing "+le.getSourceDn(),
                            le.getReason()
                    );

                    LocalData data = map.get(le.getSourceDn());
                    editEntry(data, le.getTargetDn(), le.getTargetAttributes());
                }
            }

            localTableViewer.refresh();

            globalTable.removeAll();
            globalAttributeTable.removeAll();

            if (count == 1) {
                TableItem item = localTable.getSelection()[0];
                LocalData data = (LocalData)item.getData();
                updateGlobal(data);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void editEntry(LocalData data, DN dn, Attributes attributes) {
        while (true) {
            EntryDialog dialog = new EntryDialog(getSite().getShell(), SWT.NONE);
            dialog.setText("Import Entry");
            dialog.setDn(dn);
            dialog.setAttributes(attributes);

            if (dialog.open() == EntryDialog.CANCEL) break;

            try {
                dn = dialog.getDn();
                attributes = dialog.getAttributes();
                addEntry(dn, attributes);

                data.addLink(dn);
                data.removeMatches();

                break;

            } catch (LDAPException e) {
                ErrorDialog.open(
                        "Failed adding "+dn,
                        e.getLDAPErrorMessage()
                );

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                ErrorDialog.open(e);
            }
        }
    }

    public void deleteEntries() {
        try {
            if (localTable.getSelectionCount() == 0) return;
            if (globalTable.getSelectionCount() == 0) return;

            boolean confirm = MessageDialog.openQuestion(
                    getSite().getShell(),
                    "Delete",
                    "Are you sure?"
            );

            if (!confirm) return;

            TableItem localItem = localTable.getSelection()[0];
            LocalData data = (LocalData)localItem.getData();

            DeleteTask task = new DeleteTask(this, repository);
            task.setData(data);
            task.setBaseDn(globalBaseText.getText());
            task.setFilter(globalFilterText.getText());
            task.setScope(globalScopeCombo.getSelectionIndex());

            Collection<DN> dns = new ArrayList<DN>();

            for (TableItem item : globalTable.getSelection()) {
                DN dn = (DN)item.getData("global");
                dns.add(dn);
            }

            task.setDns(dns);

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            progressService.busyCursorWhile(task);

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

    public Collection<DN> searchLinks(DN sourceDn) throws Exception {
        try {
            return (Collection<DN>)linkingModuleClient.invoke(
                    "searchLinks",
                    new Object[] { sourceDn },
                    new String[] { DN.class.getName() }
            );
        } catch (MBeanException e) {
            throw (Exception)e.getCause();
        }
    }

    public void linkEntry(DN sourceDn, DN targetDn) throws Exception {
        try {
            linkingModuleClient.invoke(
                    "linkEntry",
                    new Object[] { sourceDn, targetDn },
                    new String[] { DN.class.getName(), DN.class.getName() }
            );
        } catch (MBeanException e) {
            throw (Exception)e.getCause();
        }
    }

    public void unlinkEntry(DN sourceDn, DN targetDn) throws Exception {
        try {
            linkingModuleClient.invoke(
                    "unlinkEntry",
                    new Object[] { sourceDn, targetDn },
                    new String[] { DN.class.getName(), DN.class.getName() }
            );
        } catch (MBeanException e) {
            throw (Exception)e.getCause();
        }
    }

    public DN importEntry(DN sourceDn) throws Exception {
        try {
            return (DN)linkingModuleClient.invoke(
                    "importEntry",
                    new Object[] { sourceDn },
                    new String[] { DN.class.getName() }
            );
        } catch (MBeanException e) {
            throw (Exception)e.getCause();
        }
    }

    public void addEntry(DN targetDn, Attributes targetAttributes) throws Exception {
        try {
            linkingModuleClient.invoke(
                    "addEntry",
                    new Object[] { targetDn, targetAttributes },
                    new String[] { DN.class.getName(), Attributes.class.getName() }
            );
        } catch (MBeanException e) {
            throw (Exception)e.getCause();
        }
    }

    public void deleteEntry(DN targetDn) throws Exception {
        try {
            linkingModuleClient.invoke(
                    "deleteEntry",
                    new Object[] { targetDn },
                    new String[] { DN.class.getName() }
            );
        } catch (MBeanException e) {
            throw (Exception)e.getCause();
        }
    }
}
