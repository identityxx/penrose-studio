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
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.federation.NISDomain;
import org.safehaus.penrose.federation.Federation;
import org.safehaus.penrose.federation.SynchronizationResult;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.management.*;
import org.safehaus.penrose.module.ModuleClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;

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

    Project project;
    NISSynchronizationEditor editor;
    NISFederationClient nisFederation;
    NISDomain domain;

    Label statusText;
    Button baseButton;
    Table table;

    PartitionClient sourcePartitionClient;
    PartitionClient targetPartitionClient;

    DN sourceSuffix;
    DN targetSuffix;

    ModuleClient moduleClient;

    public NISSynchronizationPage(NISSynchronizationEditor editor) throws Exception {
        super(editor, "Content", "  Content  ");

        this.editor = editor;
        this.project = editor.getProject();
        this.nisFederation = editor.getNISFederationClient();
        this.domain = editor.getDomain();

        PenroseClient penroseClient = project.getClient();
        PartitionManagerClient partitionManagerClient = penroseClient.getPartitionManagerClient();

        sourcePartitionClient = partitionManagerClient.getPartitionClient(domain.getName()+"_"+ NISDomain.YP);
        targetPartitionClient = partitionManagerClient.getPartitionClient(domain.getName()+"_"+ NISDomain.NIS);

        sourceSuffix = sourcePartitionClient.getSuffix();
        targetSuffix = targetPartitionClient.getSuffix();

        moduleClient = targetPartitionClient.getModuleClient(Federation.SYNCHRONIZATION_MODULE);
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

        try {
            Map<String,String> nisMapRDNs = (Map<String,String>)moduleClient.getAttribute("NisMapRDNs");

            for (String nisMap : nisMapRDNs.keySet()) {
                RDN rdn = new RDN(nisMapRDNs.get(nisMap));
                String name = rdn.getNames().iterator().next();
                String label = (String)rdn.get(name);

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

            moduleClient.invoke(
                    "createBase",
                    new Object[] { },
                    new String[] { }
            );

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

            moduleClient.invoke(
                    "removeBase",
                    new Object[] { },
                    new String[] { }
            );

            statusText.setText("Missing");
            baseButton.setText("Create Base");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void refreshBase() {
        try {
            targetPartitionClient.find(targetSuffix);
            statusText.setText("Created");
            baseButton.setText("Remove Base");

        } catch (Exception e) {
            statusText.setText("Missing");
            baseButton.setText("Create Base");
        }
    }

    public void create() {
        try {
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

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Creating subtree...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                        for (String mapName : mapNames) {

                            if (monitor.isCanceled()) throw new InterruptedException();

                            monitor.subTask("Creating "+mapName+"...");

                            DN dn = getTargetDn(mapName);

                            moduleClient.invoke(
                                    "create",
                                    new Object[] { dn },
                                    new String[] { DN.class.getName() }
                            );

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
        try {
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

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Clearing...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                        for (String mapName : mapNames) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            DN dn = getTargetDn(mapName);

                            monitor.subTask("Clearing "+dn+"...");

                            moduleClient.invoke(
                                    "clear",
                                    new Object[] { dn },
                                    new String[] { DN.class.getName() }
                            );
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
        try {
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

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Removing subtree...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                        for (String mapName : mapNames) {

                            if (monitor.isCanceled()) throw new InterruptedException();

                            monitor.subTask("Removing "+mapName+"...");

                            DN dn = getTargetDn(mapName);

                            moduleClient.invoke(
                                    "remove",
                                    new Object[] { dn },
                                    new String[] { DN.class.getName() }
                            );

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
        try {
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

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Synchronizing...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                        for (String mapName : mapNames) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            DN dn = getTargetDn(mapName);
                            monitor.subTask("Synchronizing "+dn+"...");

                            SynchronizationResult result = (SynchronizationResult)moduleClient.invoke(
                                    "synchronize",
                                    new Object[] { dn },
                                    new String[] { DN.class.getName() }
                            );

                            log.warn("Synchronization Result:");
                            log.warn(" - added     : "+result.getAddedEntries());
                            log.warn(" - modified  : "+result.getModifiedEntries());
                            log.warn(" - deleted   : "+result.getDeletedEntries());
                            log.warn(" - unchanged : "+result.getUnchangedEntries());
                            log.warn(" - failed    : "+result.getFailedEntries());
                            log.warn(" - total     : "+result.getTotalEntries());
                            log.warn(" - time      : "+result.getDuration()/1000.0+" s");

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

            Map<String,String> statuses = refreshTarget(mapNames);

            for (TableItem ti : items) {
                String mapName = (String)ti.getData();

                String status = statuses.get(mapName);
                ti.setText(2, status);

                SynchronizationResult result = results.get(mapName);

                long duration = result.getDuration()/1000;
                long minutes = duration / 60;
                long seconds = duration % 60;

                ti.setText(3, minutes+":"+seconds);
                ti.setText(4, ""+result.getAddedEntries());
                ti.setText(5, ""+result.getModifiedEntries());
                ti.setText(6, ""+result.getDeletedEntries());
                ti.setText(7, ""+result.getFailedEntries());
            }

            if (totalResult.getFailedEntries() > 0) {
                ErrorDialog.open("ERROR", "Error(s) occured during synchronization. See Errors tab.");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
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
            log.debug("Searching "+dn+".");

            SearchRequest request = new SearchRequest();
            request.setDn(dn);
            request.setAttributes(new String[] { "dn" });
            request.setTypesOnly(true);

            SearchResponse response = new SearchResponse();

            sourcePartitionClient.search(request, response);

            int rc = response.waitFor();

            String status;
            if (rc == LDAP.SUCCESS) {
                status = ""+(response.getTotalCount()-1);
            } else {
                status = "N/A";
            }

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
            log.debug("Searching "+dn+".");

            SearchRequest request = new SearchRequest();
            request.setDn(dn);
            request.setAttributes(new String[] { "dn" });
            request.setTypesOnly(true);

            SearchResponse response = new SearchResponse();

            targetPartitionClient.search(request, response);

            int rc = response.waitFor();

            String status;
            if (rc == LDAP.SUCCESS) {
                status = ""+(response.getTotalCount()-1);
            } else {
                status = "N/A";
            }

            log.debug("Status: "+status);
            
            return status;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "N/A";
        }
    }
}
