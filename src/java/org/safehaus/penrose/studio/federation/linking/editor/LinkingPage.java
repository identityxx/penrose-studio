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
import org.safehaus.penrose.federation.*;
import org.safehaus.penrose.federation.module.LinkingException;
import org.safehaus.penrose.federation.LinkingClient;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.FilterTool;
import org.safehaus.penrose.filter.SubstringFilter;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.ldap.dialog.EntryDialog;
import org.safehaus.penrose.studio.federation.linking.*;
import org.safehaus.penrose.studio.federation.wizard.BrowserWizard;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.util.ActiveDirectoryUtil;
import org.safehaus.penrose.util.BinaryUtil;
import org.safehaus.penrose.source.SourceClient;

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

    public FormToolkit toolkit;

    public Text localBaseText;
    public Text localFilterText;
    public Combo localScopeCombo;

    public TableViewer localTableViewer;
    public Table localTable;
    public TableColumn localDnTableColumn;
    public TableColumn localStatusTableColumn;
    public Table localAttributeTable;

    public Text globalBaseText;
    public Text globalFilterText;
    public Combo globalScopeCombo;

    public Table globalTable;
    public Table globalAttributeTable;

    public Project project;
    public Repository repository;
    public String partitionName;

    public PartitionClient localPartitionClient;
    public PartitionClient globalPartitionClient;

    public SourceClient localSourceClient;
    public SourceClient globalSourceClient;

    public LinkingClient linkingClient;

    public DN localBaseDn;
    public DN globalBaseDn;

    public DNSorter dnSorter         = new DNSorter();
    public StatusSorter statusSorter = new StatusSorter();

    public LinkingPage(LinkingEditor editor) throws Exception {
        super(editor, "LINKING", "  Linking  ");

        LinkingEditorInput ei = (LinkingEditorInput)editor.getEditorInput();

        this.project = ei.getProject();
        this.repository = ei.getRepository();
        this.partitionName = ei.getPartitionName();

        PenroseClient penroseClient = project.getClient();

        PartitionManagerClient partitionManagerClient = penroseClient.getPartitionManagerClient();
        localPartitionClient = partitionManagerClient.getPartitionClient(partitionName);
        globalPartitionClient = partitionManagerClient.getPartitionClient(FederationClient.GLOBAL);

        localSourceClient = localPartitionClient.getSourceClient("LDAP");
        globalSourceClient = globalPartitionClient.getSourceClient("LDAP");

        //localBaseDn = localPartitionClient.getSuffix();
        localBaseDn = new DN(localSourceClient.getParameter("baseDn"));

        //globalBaseDn = globalPartitionClient.getSuffix();
        globalBaseDn = new DN(globalSourceClient.getParameter("baseDn"));

        linkingClient = new LinkingClient(penroseClient, partitionName, Federation.LINKING_MODULE);
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
                browseLocalBaseDn();
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
                selectLocalEntry();
            }
        });

        localDnTableColumn = new TableColumn(localTable, SWT.NONE);
        localDnTableColumn.setText("DN");
        localDnTableColumn.setWidth(230);

        localDnTableColumn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                sortByDn();
            }
        });

        localStatusTableColumn = new TableColumn(localTable, SWT.NONE);
        localStatusTableColumn.setText("Status");
        localStatusTableColumn.setWidth(70);

        localStatusTableColumn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                sortByStatus();
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

        new MenuItem(menu, SWT.SEPARATOR);

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Refresh");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                refreshLocalEntries();
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
                browseGlobalBaseDn();
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
                selectGlobalEntry();
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
            clearLocalAttributes();
            globalTable.removeAll();
            clearGlobalAttributes();

            final SearchRequest request = new SearchRequest();
            request.setDn(localBaseText.getText());
            request.setFilter(localFilterText.getText());
            request.setScope(localScopeCombo.getSelectionIndex());

            final Collection<LinkingData> results = new ArrayList<LinkingData>();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Searching "+ repository.getName()+"...", IProgressMonitor.UNKNOWN);

                        Collection<LinkingData> list = linkingClient.search(request);

                        for (LinkingData data : list) {
                            updateStatus(data);
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
            clearGlobalAttributes();

            TableItem items[] = localTable.getSelection();
            if (items.length == 0) items = localTable.getItems();

            final Collection<LinkingData> results = new ArrayList<LinkingData>();

            for (TableItem item : items) {
                LinkingData data = (LinkingData)item.getData();
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

                        for (LinkingData data : results) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            DN dn = data.getDn();
                            monitor.subTask("Processing "+dn+"...");

                            Collection<SearchResult> linkedEntries = data.getLinkedEntries();

                            if (!linkedEntries.isEmpty()) {
                                monitor.worked(1);
                                continue;
                            }

                            data.removeMatchedEntries();

                            Filter f = createFilter(data.getEntry(), filter);
                            Collection<SearchResult> matchedEntries = searchMatches(baseDn, f, scope);
                            
                            log.debug("Found "+matchedEntries.size()+" matches:");
                            for (SearchResult matchedEntry : matchedEntries) {
                                log.debug(" - "+matchedEntry.getDn());
                                data.addMatchedEntry(matchedEntry);
                            }

                            data.setSearched(true);

                            updateStatus(data);

                            monitor.worked(1);
                        }

                    } catch (InterruptedException e) {
                        // ignore

                    } catch (MBeanException e) {
                        throw new InvocationTargetException(e.getCause());

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
                LinkingData data = (LinkingData)item.getData();
                showGlobalEntries(data);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void showGlobalEntries(LinkingData data) throws Exception {

        Collection<SearchResult> linkedEntries = data.getLinkedEntries();

        if (!linkedEntries.isEmpty()) {
            for (SearchResult linkedEntry : linkedEntries) {

                TableItem ti = new TableItem(globalTable, SWT.NONE);
                ti.setText(0, linkedEntry.getDn().toString());
                ti.setData(linkedEntry);

                if (linkedEntries.size() == 1) {
                    showGlobalAttributes(linkedEntry);
                }
            }

            return;
        }

        Collection<SearchResult> matchedEntries = data.getMatchedEntries();
        if (matchedEntries.isEmpty()) return;

        for (SearchResult matchedEntry : matchedEntries) {

            TableItem ti = new TableItem(globalTable, SWT.NONE);
            ti.setText(0, matchedEntry.getDn().toString());
            ti.setData(matchedEntry);

            if (matchedEntries.size() == 1) {
                showGlobalAttributes(matchedEntry);
            }
        }
    }

    public void updateLocal(LinkingData data) throws Exception {
        try {
            //SearchResult entry = localPartitionClient.find(data.getDn());
            SearchResult entry = localSourceClient.find(data.getDn());
            data.setEntry(entry);
            updateStatus(data);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void clearLocalAttributes() {
        localAttributeTable.removeAll();
    }

    public void clearGlobalAttributes() {
        globalAttributeTable.removeAll();
    }

    public void showLocalAttributes(LinkingData data) throws Exception {
        updateAttributes(localAttributeTable, data.getEntry());
    }

    public void showGlobalAttributes(SearchResult entry) throws Exception {
        updateAttributes(globalAttributeTable, entry);
    }

    public void updateAttributes(Table table, SearchResult entry) throws Exception {

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

                } else if ("adiSeeAlsoObjectGUID".equalsIgnoreCase(attributeName)) {
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

    public Collection<SearchResult> searchMatches(String baseDn, Filter filter, int scope) throws Exception {
        
        if (filter == null) return null;

        SearchRequest request = new SearchRequest();
        request.setDn(baseDn);
        request.setFilter(filter);
        request.setScope(scope);

        SearchResponse response = new SearchResponse();

        //globalPartitionClient.search(request, response);
        globalSourceClient.search(request, response);

        Collection<SearchResult> results = new ArrayList<SearchResult>();

        while (response.hasNext()) {
            SearchResult result = response.next();
            results.add(result);
        }

        return results;
    }

    public void browseLocalBaseDn() {
        try {
            BrowserWizard wizard = new BrowserWizard();
            wizard.setBaseDn(localBaseDn);
            wizard.setDn(localBaseText.getText());
            //wizard.setPartitionClient(localPartitionClient);
            wizard.setSourceClient(localSourceClient);

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

    public void browseGlobalBaseDn() {
        try {
            BrowserWizard wizard = new BrowserWizard();
            wizard.setBaseDn(globalBaseDn);
            wizard.setDn(globalBaseText.getText());
            //wizard.setPartitionClient(globalPartitionClient);
            wizard.setSourceClient(globalSourceClient);

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

    public void selectLocalEntry() {
        try {
            clearLocalAttributes();
            globalTable.removeAll();
            clearGlobalAttributes();

            if (localTable.getSelectionCount() != 1) return;

            TableItem item = localTable.getSelection()[0];

            LinkingData data = (LinkingData)item.getData();
            showLocalAttributes(data);
            showGlobalEntries(data);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void refreshLocalEntries() {
        try {
            TableItem[] items = localTable.getSelection();
            if (items == null || items.length == 0) items = localTable.getItems();

            for (TableItem item : items) {
                LinkingData data = (LinkingData)item.getData();
                data.setSearched(false);
                data.removeMatchedEntries();
                data.removeLinkedEntries();
                updateLocal(data);
            }

            localTableViewer.refresh();
            clearLocalAttributes();

            globalTable.removeAll();
            clearGlobalAttributes();

            if (items.length != 1) return;

            TableItem item = items[0];

            LinkingData data = (LinkingData)item.getData();
            showLocalAttributes(data);
            showGlobalEntries(data);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void selectGlobalEntry() {
        try {
            if (globalTable.getSelectionCount() != 1) return;

            clearGlobalAttributes();

            TableItem globalItem = globalTable.getSelection()[0];
            SearchResult globalEntry = (SearchResult)globalItem.getData();

            showGlobalAttributes(globalEntry);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void sortByDn() {
        if (localTable.getSortColumn() == localDnTableColumn) {
            dnSorter.invert();

        } else {
            localTable.setSortColumn(localDnTableColumn);
            localTableViewer.setSorter(dnSorter);
        }

        localTable.setSortDirection(dnSorter.getDirection());
        localTableViewer.refresh();
    }

    public void sortByStatus() {
        if (localTable.getSortColumn() == localStatusTableColumn) {
            statusSorter.invert();

        } else {
            localTable.setSortColumn(localStatusTableColumn);
            localTableViewer.setSorter(statusSorter);
        }

        localTable.setSortDirection(statusSorter.getDirection());
        localTableViewer.refresh();
    }

    public void linkLocalEntries() {
        try {
            int count = localTable.getSelectionCount();
            if (count == 0) return;

            if (count == 1) {
                TableItem item = localTable.getSelection()[0];
                LinkingData data = (LinkingData)item.getData();

                Collection<SearchResult> linkedEntries = data.getLinkedEntries();

                if (!linkedEntries.isEmpty()) {
                    ErrorDialog.open("This account has been linked.");
                    return;
                }

                Collection<SearchResult> matchedEntries = data.getMatchedEntries();

                if (matchedEntries.isEmpty()) {
                    ErrorDialog.open("Find a global identity to link to this account.");
                    return;
                }

                if (matchedEntries.size() > 1) {
                    ErrorDialog.open(
                            "Cannot link multiple global identities.\n"+
                            "Right click on one of the global identities, then select Link."
                    );
                    return;
                }

                SearchResult matchedEntry = matchedEntries.iterator().next();
                linkingClient.linkEntry(data.getDn(), matchedEntry.getDn());

                data.removeMatchedEntries();
                data.removeLinkedEntries();

                if (LinkingData.LOCAL_STORAGE == data.getStorage()) {
                    //SearchResult entry = localPartitionClient.find(data.getDn());
                    SearchResult entry = localSourceClient.find(data.getDn());
                    data.setEntry(entry);
                    data.addLinkedEntry(matchedEntry);

                } else {
                    //SearchResult linkedEntry = globalPartitionClient.find(matchedEntry.getDn());
                    SearchResult linkedEntry = globalSourceClient.find(matchedEntry.getDn());
                    data.addLinkedEntry(linkedEntry);
                }

                updateStatus(data);

                clearLocalAttributes();
                updateLocal(data);
                showLocalAttributes(data);

            } else {

                for (TableItem item : localTable.getSelection()) {
                    LinkingData data = (LinkingData)item.getData();

                    Collection<SearchResult> linkedEntries = data.getLinkedEntries();
                    if (!linkedEntries.isEmpty()) continue;

                    Collection<SearchResult> matchedEntries = data.getMatchedEntries();
                    if (matchedEntries.isEmpty() || matchedEntries.size() > 1) continue;

                    SearchResult matchedEntry = matchedEntries.iterator().next();
                    linkingClient.linkEntry(data.getDn(), matchedEntry.getDn());

                    data.removeMatchedEntries();
                    data.removeLinkedEntries();

                    if (LinkingData.LOCAL_STORAGE == data.getStorage()) {
                        //SearchResult entry = localPartitionClient.find(data.getDn());
                        SearchResult entry = localSourceClient.find(data.getDn());
                        data.setEntry(entry);
                        data.addLinkedEntry(matchedEntry);

                    } else {
                        //SearchResult linkedEntry = globalPartitionClient.find(matchedEntry.getDn());
                        SearchResult linkedEntry = globalSourceClient.find(matchedEntry.getDn());
                        data.addLinkedEntry(linkedEntry);
                    }

                    updateStatus(data);
                }
            }

            localTableViewer.refresh();

        } catch (MBeanException e) {
            Exception ex = (Exception)e.getCause();
            log.error(ex.getMessage(), ex);
            ErrorDialog.open(ex);

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

            LinkingData data = (LinkingData)localItem.getData();

            SearchResult matchedEntry = (SearchResult)globalItem.getData();

            linkingClient.linkEntry(data.getDn(), matchedEntry.getDn());

            data.removeMatchedEntries();
            data.removeLinkedEntries();

            if (LinkingData.LOCAL_STORAGE == data.getStorage()) {
                //SearchResult entry = localPartitionClient.find(data.getDn());
                SearchResult entry = localSourceClient.find(data.getDn());
                data.setEntry(entry);
                data.addLinkedEntry(matchedEntry);

            } else {
                //SearchResult linkedEntry = globalPartitionClient.find(matchedEntry.getDn());
                SearchResult linkedEntry = globalSourceClient.find(matchedEntry.getDn());
                data.addLinkedEntry(linkedEntry);
            }

            updateStatus(data);

            localTableViewer.refresh();
            clearLocalAttributes();
            updateLocal(data);
            showLocalAttributes(data);

            globalTable.removeAll();
            clearGlobalAttributes();
            showGlobalEntries(data);

        } catch (MBeanException e) {
            Exception ex = (Exception)e.getCause();
            log.error(ex.getMessage(), ex);
            ErrorDialog.open(ex);

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

            for (TableItem item : localTable.getSelection()) {
                LinkingData data = (LinkingData)item.getData();

                Collection<SearchResult> linkedEntries = data.getLinkedEntries();
                if (linkedEntries.isEmpty()) continue;

                if (globalTable.getSelectionCount() == 0) {

                    for (SearchResult linkedEntry : linkedEntries) {
                        linkingClient.unlinkEntry(data.getDn(), linkedEntry.getDn());
                    }
                    data.removeLinkedEntries();

                } else {
                    for (TableItem globalItem : globalTable.getSelection()) {
                        SearchResult linkedEntry = (SearchResult)globalItem.getData();
                        linkingClient.unlinkEntry(data.getDn(), linkedEntry.getDn());

                        data.removeLinkedEntry(linkedEntry.getDn());
                    }
                }

                if (LinkingData.LOCAL_STORAGE == data.getStorage()) {
                    //SearchResult entry = localPartitionClient.find(data.getDn());
                    SearchResult entry = localSourceClient.find(data.getDn());
                    data.setEntry(entry);
                }

                data.setSearched(false);
                updateLocal(data);
            }

            if (count == 1) {
                TableItem item = localTable.getSelection()[0];
                LinkingData data = (LinkingData)item.getData();

                localTableViewer.refresh();
                clearLocalAttributes();
                //updateLocal(data);
                showLocalAttributes(data);

                globalTable.removeAll();
                clearGlobalAttributes();
                showGlobalEntries(data);

            } else {
                localTableViewer.refresh();
                globalTable.removeAll();
                clearGlobalAttributes();
            }

        } catch (MBeanException e) {
            Exception ex = (Exception)e.getCause();
            log.error(ex.getMessage(), ex);
            ErrorDialog.open(ex);

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

            Map<DN, LinkingData> map = new HashMap<DN, LinkingData>();
            java.util.List<LinkingData> results = new ArrayList<LinkingData>();

            for (TableItem item : localTable.getSelection()) {
                LinkingData data = (LinkingData)item.getData();

                Collection<SearchResult> linkedEntries = data.getLinkedEntries();
                if (!linkedEntries.isEmpty()) return;

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

                    LinkingData data = map.get(le.getSourceDn());
                    int rc = editEntry(data, le.getTargetDn(), le.getTargetAttributes());
                    if (rc == EntryDialog.CANCEL) break;
                }
            }

            if (count == 1) {
                TableItem item = localTable.getSelection()[0];
                LinkingData data = (LinkingData)item.getData();

                localTableViewer.refresh();
                clearLocalAttributes();
                showLocalAttributes(data);

                globalTable.removeAll();
                clearGlobalAttributes();
                showGlobalEntries(data);

            } else {
                localTableViewer.refresh();
                globalTable.removeAll();
                clearGlobalAttributes();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public int editEntry(LinkingData data, DN targetDn, Attributes targetAttributes) {
        while (true) {
            EntryDialog dialog = new EntryDialog(getSite().getShell(), SWT.NONE);
            dialog.setText("Import Entry");
            dialog.setDn(targetDn);
            dialog.setAttributes(targetAttributes);

            dialog.addGUIDAttribute("objectGUID");
            dialog.addGUIDAttribute("adiSeeAlsoObjectGUID");
            dialog.addSIDAttribute("objectSid");

            int rc = dialog.open();
            if (rc == EntryDialog.SKIP || rc == EntryDialog.CANCEL) return rc;

            try {
                targetDn = dialog.getDn();
                targetAttributes = dialog.getAttributes();

                SearchResult newEntry = new SearchResult(targetDn, targetAttributes);

                newEntry = linkingClient.importEntry(data.getDn(), newEntry);

                data.setSearched(false);
                data.removeMatchedEntries();
                data.removeLinkedEntries();
                data.addLinkedEntry(newEntry);
                updateLocal(data);

                break;

            } catch (LDAPException e) {
                ErrorDialog.open(
                        "Failed adding "+targetDn,
                        e.getLDAPErrorMessage()
                );

            } catch (MBeanException e) {
                Exception ex = (Exception)e.getCause();
                log.error(ex.getMessage(), ex);
                ErrorDialog.open(ex);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                ErrorDialog.open(e);
            }
        }

        return EntryDialog.OK;
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
            LinkingData data = (LinkingData)localItem.getData();

            DeleteTask task = new DeleteTask(this, repository);
            task.setData(data);
            task.setBaseDn(globalBaseText.getText());
            task.setFilter(globalFilterText.getText());
            task.setScope(globalScopeCombo.getSelectionIndex());

            Collection<SearchResult> globalEntries = new ArrayList<SearchResult>();

            for (TableItem item : globalTable.getSelection()) {
                SearchResult globalEntry = (SearchResult)item.getData();
                globalEntries.add(globalEntry);
            }

            task.setGlobalEntries(globalEntries);

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            progressService.busyCursorWhile(task);

            localTableViewer.refresh();

            for (TableItem item : globalTable.getSelection()) {
                item.dispose();
            }

            globalTable.removeAll();
            clearGlobalAttributes();

            showGlobalEntries(data);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void updateStatus(LinkingData data) {

        Collection<SearchResult> linkedEntries = data.getLinkedEntries();
        if (!linkedEntries.isEmpty()) {

            if (linkedEntries.size() == 1) {
                data.setStatus("Linked");
                return;
            }

            data.setStatus(linkedEntries.size()+" Links");
            return;
        }

        if (!data.isSearched()) {
            data.setStatus(null);
            return;
        }

        Collection<SearchResult> matchedEntries = data.getMatchedEntries();
        if (!matchedEntries.isEmpty()) {

            if (matchedEntries.size() == 1) {
                data.setStatus("1 Match");
                return;

            } else {
                data.setStatus(matchedEntries.size()+" Matches");
                return;
            }
        }

        data.setStatus("Not Found");
    }
}
