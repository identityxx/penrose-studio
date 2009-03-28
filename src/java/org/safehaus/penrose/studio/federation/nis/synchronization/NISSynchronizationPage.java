package org.safehaus.penrose.studio.federation.nis.synchronization;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.apache.log4j.Logger;
import org.safehaus.penrose.federation.*;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.synchronization.SynchronizationResult;
import org.safehaus.penrose.nis.NISSynchronizationModuleClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Endi S. Dewata
 */
public class NISSynchronizationPage extends FormPage {

    public DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Server server;
    NISSynchronizationEditor editor;
    NISRepositoryClient nisFederationClient;
    FederationRepositoryConfig domain;

    Label statusText;
    Button baseButton;
    Table table;

    PartitionClient sourcePartitionClient;
    PartitionClient targetPartitionClient;

    SourceClient sourceClient;
    SourceClient targetClient;

    DN sourceSuffix;
    DN targetSuffix;

    NISSynchronizationModuleClient moduleClient;

    public NISSynchronizationPage(NISSynchronizationEditor editor) throws Exception {
        super(editor, "Content", "  Content  ");

        this.editor = editor;
        this.server = editor.getServer();
        this.nisFederationClient = editor.getNISFederationClient();
        this.domain = editor.getDomain();

        PenroseClient penroseClient = server.getClient();
        PartitionManagerClient partitionManagerClient = penroseClient.getPartitionManagerClient();

        String federationName = nisFederationClient.getFederationClient().getFederationDomain();

        sourcePartitionClient = partitionManagerClient.getPartitionClient(federationName+"_"+domain.getName()+"_"+ NISDomain.YP);
        targetPartitionClient = partitionManagerClient.getPartitionClient(federationName+"_"+domain.getName());

        SourceManagerClient sourceSourceManagerClient = sourcePartitionClient.getSourceManagerClient();
        sourceClient  = sourceSourceManagerClient.getSourceClient("LDAP");

        SourceManagerClient targetSourceManagerClient = targetPartitionClient.getSourceManagerClient();
        targetClient  = targetSourceManagerClient.getSourceClient("LDAP");

        sourceSuffix = (DN)sourceClient.getAttribute("BaseDn");
        targetSuffix = (DN)targetClient.getAttribute("BaseDn");

        moduleClient = new NISSynchronizationModuleClient(penroseClient, federationName+"_"+domain.getName(), Federation.SYNCHRONIZATION);
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Synchronization");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section baseSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        baseSection.setText("Base");
        baseSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control baseControl = createBaseSection(baseSection);
        baseSection.setClient(baseControl);

        Section contentSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        contentSection.setText("Content");
        contentSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control contentControl = createContentSection(contentSection);
        contentSection.setClient(contentControl);

        refreshBase();
    }

    public Composite createBaseSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftPanel = toolkit.createComposite(composite);
        leftPanel.setLayout(new GridLayout(2, false));
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label sourceLabel = toolkit.createLabel(leftPanel, "Source:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        sourceLabel.setLayoutData(gd);

        Label sourceText = toolkit.createLabel(leftPanel, sourceSuffix.toString());
        sourceText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label targetLabel = toolkit.createLabel(leftPanel, "Target:");
        targetLabel.setLayoutData(new GridData());

        Label targetText = toolkit.createLabel(leftPanel, targetSuffix.toString());
        targetText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label statusLabel = toolkit.createLabel(leftPanel, "Status:");
        gd = new GridData();
        gd.widthHint = 80;
        statusLabel.setLayoutData(gd);

        statusText = toolkit.createLabel(leftPanel, "");
        statusText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite rightPanel = toolkit.createComposite(composite);
        rightPanel.setLayout(new GridLayout());
        gd = new GridData(GridData.FILL_VERTICAL);
        gd.verticalSpan = 2;
        gd.widthHint = 120;
        rightPanel.setLayoutData(gd);

        baseButton = new Button(rightPanel, SWT.PUSH);
        baseButton.setText("Create Base");
        baseButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        baseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                if ("Created".equals(statusText.getText())) {
                    removeBase();
                } else {
                    createBase();
                }
            }
        });

        Button refreshButton = new Button(rightPanel, SWT.PUSH);
        refreshButton.setText("Refresh");
        refreshButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                refreshBase();
            }
        });

        return composite;
    }

    public Composite createContentSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftPanel = toolkit.createComposite(composite);
        leftPanel.setLayout(new GridLayout());
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        table = new Table(leftPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Name");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(80);
        tc.setText("Source");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(80);
        tc.setText("Target");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(70);
        tc.setText("Duration");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(50);
        tc.setText("Added");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(60);
        tc.setText("Modified");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(60);
        tc.setText("Deleted");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(50);
        tc.setText("Failed");

        Menu menu = new Menu(table);
        table.setMenu(menu);

        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Refresh Source");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                refreshSource();
            }
        });

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Refresh Target");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                refreshTarget();
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Create");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                create();
            }
        });

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Clear");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                clear();
            }
        });

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Remove");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                remove();
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Synchronize");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                synchronize();
            }
        });

        try {
            Map<String,String> nisMapRDNs = moduleClient.getNisMapRDNs();

            for (String nisMap : nisMapRDNs.keySet()) {
                RDN rdn = new RDN(nisMapRDNs.get(nisMap));
                String label = (String)rdn.getValue();

                TableItem ti = new TableItem(table, SWT.NONE);
                ti.setText(0, label);
                ti.setData(label);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }

        Composite links = toolkit.createComposite(leftPanel);
        links.setLayout(new RowLayout());
        links.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Hyperlink selectAllLink = toolkit.createHyperlink(links, "Select All", SWT.NONE);

        selectAllLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                table.selectAll();
            }
        });

        Hyperlink selectNoneLink = toolkit.createHyperlink(links, "Select None", SWT.NONE);

        selectNoneLink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                table.deselectAll();
            }
        });

        Composite rightPanel = toolkit.createComposite(composite);
        rightPanel.setLayout(new GridLayout());
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.verticalSpan = 2;
        gd.widthHint = 120;
        rightPanel.setLayoutData(gd);

        Button refreshSourceButton = new Button(rightPanel, SWT.PUSH);
        refreshSourceButton.setText("Refresh Source");
        refreshSourceButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        refreshSourceButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                refreshSource();
            }
        });

        Button refreshTargetButton = new Button(rightPanel, SWT.PUSH);
        refreshTargetButton.setText("Refresh Target");
        refreshTargetButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        refreshTargetButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                refreshTarget();
            }
        });

        new Label(rightPanel, SWT.NONE);

        Button createButton = new Button(rightPanel, SWT.PUSH);
        createButton.setText("Create");
        createButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                create();
            }
        });

        Button clearButton = new Button(rightPanel, SWT.PUSH);
        clearButton.setText("Clear");
        clearButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        clearButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                clear();
            }
        });

        Button removeButton = new Button(rightPanel, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                remove();
            }
        });

        new Label(rightPanel, SWT.NONE);

        Button synchronizeButton = new Button(rightPanel, SWT.PUSH);
        synchronizeButton.setText("Synchronize");
        synchronizeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        synchronizeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                synchronize();
            }
        });

        return composite;
    }

    public void createBase() {
        try {
            boolean confirm = MessageDialog.openQuestion(
                    editor.getSite().getShell(),
                    "Creating base entry",
                    "Are you sure?"
            );

            if (!confirm) return;

            moduleClient.createBase();

            statusText.setText("Created");
            baseButton.setText("Remove Base");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void removeBase() {
        try {
            boolean confirm = MessageDialog.openQuestion(
                    editor.getSite().getShell(),
                    "Removing base entry",
                    "Are you sure?"
            );

            if (!confirm) return;

            moduleClient.removeBase();

            statusText.setText("Missing");
            baseButton.setText("Create Base");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void refreshBase() {
        try {
            targetClient.find(targetSuffix);
            statusText.setText("Created");
            baseButton.setText("Remove Base");

        } catch (Exception e) {
            statusText.setText("Missing");
            baseButton.setText("Create Base");
        }
    }

    public void create() {
        if (table.getSelectionCount() == 0) return;

        boolean confirm = MessageDialog.openQuestion(
                editor.getSite().getShell(),
                "Creating Target Subtree",
                "Are you sure?"
        );

        if (!confirm) return;

        TableItem[] items = table.getSelection();

        final Collection<String> mapNames = new ArrayList<String>();

        for (TableItem item : items) {
            String mapName = (String)item.getData();
            mapNames.add(mapName);
        }

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        try {
            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Creating subtree...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                        for (String mapName : mapNames) {

                            if (monitor.isCanceled()) throw new InterruptedException();

                            monitor.subTask("Creating "+mapName+"...");

                            DN dn = getTargetDn(mapName);
                            moduleClient.create(dn);

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

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }

        try {

            Map<String,String> statuses = refreshTarget(mapNames);

            for (TableItem ti : items) {
                String mapName = (String)ti.getData();

                String status = statuses.get(mapName);
                ti.setText(2, status);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void clear() {
        if (table.getSelectionCount() == 0) return;

        boolean confirm = MessageDialog.openQuestion(
                editor.getSite().getShell(),
                "Clearing Target Subtree",
                "Are you sure?"
        );

        if (!confirm) return;

        TableItem[] items = table.getSelection();

        final Collection<String> mapNames = new ArrayList<String>();

        for (TableItem ti : items) {
            String mapName = (String)ti.getData();
            mapNames.add(mapName);
        }

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        try {
            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Clearing...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                        for (String mapName : mapNames) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            monitor.subTask("Clearing "+mapName+"...");

                            DN dn = getTargetDn(mapName);
                            moduleClient.clear(dn);
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

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }

        try {
            Map<String,String> statuses = refreshTarget(mapNames);

            for (TableItem ti : items) {
                String mapName = (String)ti.getData();

                String status = statuses.get(mapName);
                ti.setText(2, status);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void remove() {
        if (table.getSelectionCount() == 0) return;

        boolean confirm = MessageDialog.openQuestion(
                editor.getSite().getShell(),
                "Removing Target Subtree",
                "Are you sure?"
        );

        if (!confirm) return;

        TableItem[] items = table.getSelection();

        final Collection<String> mapNames = new ArrayList<String>();

        for (TableItem item : items) {
            String mapName = (String)item.getData();
            mapNames.add(mapName);
        }

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        try {
            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Removing subtree...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                        for (String mapName : mapNames) {

                            if (monitor.isCanceled()) throw new InterruptedException();

                            monitor.subTask("Removing "+mapName+"...");

                            DN dn = getTargetDn(mapName);
                            moduleClient.remove(dn);

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

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }

        try {
            Map<String,String> statuses = refreshTarget(mapNames);

            for (TableItem ti : items) {
                String mapName = (String)ti.getData();

                String status = statuses.get(mapName);
                ti.setText(2, status);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void synchronize() {
        boolean confirm = MessageDialog.openQuestion(
                editor.getSite().getShell(),
                "Synchronize",
                "Are you sure?"
        );

        if (!confirm) return;

        TableItem[] items = table.getSelection();
        if (items == null || items.length == 0) items = table.getItems();

        final Collection<String> mapNames = new ArrayList<String>();
        for (TableItem ti : items) {
            String mapName = (String)ti.getData();
            mapNames.add(mapName);
        }

        final Map<String,SynchronizationResult> results = new HashMap<String,SynchronizationResult>();
        final SynchronizationResult totalResult = new SynchronizationResult();

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        try {
            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Synchronizing...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                        for (String mapName : mapNames) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            DN dn = getTargetDn(mapName);
                            monitor.subTask("Synchronizing "+dn+"...");

                            SynchronizationResult result = moduleClient.synchronize(dn);
                            log.warn(result.toString());

                            results.put(mapName, result);
                            totalResult.add(result);

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

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }

        //Map<String,String> statuses = refreshTarget(mapNames);

        for (TableItem ti : items) {
            String mapName = (String)ti.getData();

            //String status = statuses.get(mapName);
            //ti.setText(2, status);

            SynchronizationResult result = results.get(mapName);

            long duration = result.getDuration()/1000;
            long minutes = duration / 60;
            long seconds = duration % 60;

            Long sourceEntries = result.getSourceEntries();
            Long targetEntries = result.getTargetEntries();

            ti.setText(1, (sourceEntries == null ? "N/A" : ""+sourceEntries));
            ti.setText(2, (targetEntries == null ? "N/A" : ""+targetEntries));
            ti.setText(3, minutes+":"+seconds);
            ti.setText(4, ""+result.getAddedEntries());
            ti.setText(5, ""+result.getModifiedEntries());
            ti.setText(6, ""+result.getDeletedEntries());
            ti.setText(7, ""+result.getFailedEntries());
        }

        if (totalResult.getFailedEntries() > 0) {
            ErrorDialog.open("ERROR", "Error(s) occured during synchronization. See Errors tab.");
        }
    }

    public void refreshSource() {
        try {
            TableItem[] items = table.getSelection();
            if (items.length == 0) items = table.getItems();

            Collection<String> mapNames = new ArrayList<String>();

            for (TableItem ti : items) {
                String mapName = (String)ti.getData();
                mapNames.add(mapName);
            }

            Map<String,String> statuses = refreshSource(mapNames);

            for (TableItem ti : items) {
                String mapName = (String)ti.getData();

                String status = statuses.get(mapName);
                ti.setText(1, status == null ? "" : status);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void refreshTarget() {
        try {
            TableItem[] items = table.getSelection();
            if (items.length == 0) items = table.getItems();

            Collection<String> mapNames = new ArrayList<String>();

            for (TableItem ti : items) {
                String mapName = (String)ti.getData();
                mapNames.add(mapName);
            }

            Map<String,String> statuses = refreshTarget(mapNames);

            for (TableItem ti : items) {
                String mapName = (String)ti.getData();

                String status = statuses.get(mapName);
                ti.setText(2, status == null ? "" : status);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public Map<String,String> refreshSource(final Collection<String> mapNames) throws Exception {

        final Map<String,String> statuses = new LinkedHashMap<String,String>();

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        progressService.busyCursorWhile(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    monitor.beginTask("Refreshing...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                    for (String mapName : mapNames) {

                        if (monitor.isCanceled()) throw new InterruptedException();

                        monitor.subTask("Checking "+mapName+"...");

                        statuses.put(mapName, getSourceStatus(mapName));

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

        return statuses;
    }

    public Map<String,String> refreshTarget(final Collection<String> mapNames) throws Exception {

        final Map<String,String> statuses = new LinkedHashMap<String,String>();

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        progressService.busyCursorWhile(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    monitor.beginTask("Refreshing...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                    for (String mapName : mapNames) {

                        if (monitor.isCanceled()) throw new InterruptedException();

                        monitor.subTask("Checking "+mapName+"...");

                        statuses.put(mapName, getTargetStatus(mapName));

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

        return statuses;
    }

    public DN getSourceDn(String mapName) throws Exception {
        RDNBuilder rb = new RDNBuilder();
        rb.set("ou", mapName);
        RDN rdn = rb.toRdn();

        DNBuilder db = new DNBuilder();
        db.append(rdn);
        db.append(sourceSuffix);

        return db.toDn();
    }

    public DN getTargetDn(String mapName) throws Exception {
        RDNBuilder rb = new RDNBuilder();
        rb.set("ou", mapName);
        RDN rdn = rb.toRdn();

        DNBuilder db = new DNBuilder();
        db.append(rdn);
        db.append(targetSuffix);

        return db.toDn();
    }

    public String getSourceStatus(String mapName) {
        try {
            DN dn = getSourceDn(mapName);
            Long result = moduleClient.getSourceCount(dn);

            String status = result == null ? "N/A" : ""+result;
            log.debug("Status: "+status);

            return status;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "N/A";
        }
    }

    public String getTargetStatus(String mapName) {
        try {
            DN dn = getTargetDn(mapName);
            Long result = moduleClient.getTargetCount(dn);

            String status = result == null ? "N/A" : ""+result;
            log.debug("Status: "+status);
            
            return status;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "N/A";
        }
    }
}
