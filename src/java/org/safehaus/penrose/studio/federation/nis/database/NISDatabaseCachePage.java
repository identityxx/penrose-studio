package org.safehaus.penrose.studio.federation.nis.database;

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
import org.safehaus.penrose.federation.NISDomain;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.management.*;
import org.safehaus.penrose.module.ModuleClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.nis.NIS;

import java.util.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Endi S. Dewata
 */
public class NISDatabaseCachePage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table table;

    NISDatabaseEditor editor;
    NISDomain domain;
    NISFederationClient nisFederation;

    Project project;

    PartitionClient partitionClient;
    ModuleClient moduleClient;

    public NISDatabaseCachePage(NISDatabaseEditor editor) throws Exception {
        super(editor, "DATABASE", "  Database  ");

        this.editor = editor;
        this.project = editor.getProject();
        this.nisFederation = editor.getNisFederation();
        this.domain = editor.getDomain();

        PenroseClient penroseClient = project.getClient();

        PartitionManagerClient partitionManagerClient = penroseClient.getPartitionManagerClient();
        partitionClient = partitionManagerClient.getPartitionClient(domain.getName()+"_"+ NISDomain.DB);
        moduleClient = partitionClient.getModuleClient("NISDBSyncModule");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Database");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Database");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control mainSection = createMainSection(section);
        section.setClient(mainSection);

        refresh();
    }

    public Composite createMainSection(Composite parent) {

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
        tc.setWidth(150);
        tc.setText("Name");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Entries");
        tc.setAlignment(SWT.RIGHT);

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
        gd.widthHint = 120;
        rightPanel.setLayoutData(gd);

        Button createButton = new Button(rightPanel, SWT.PUSH);
        createButton.setText("Create");
        createButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Creating Cache",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    final Collection<String> mapNames = new ArrayList<String>();

                    for (TableItem item : items) {
                        String mapName = (String)item.getData("mapName");
                        mapNames.add(mapName);
                    }

                    IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

                    progressService.busyCursorWhile(new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor) throws InvocationTargetException {
                            try {
                                monitor.beginTask("Creating cache...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                                for (String mapName : mapNames) {

                                    if (monitor.isCanceled()) throw new InterruptedException();

                                    monitor.subTask("Creating "+mapName+"...");

                                    DN dn = getDn(mapName);

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

                    for (TableItem item : items) {
                        String mapName = (String)item.getData("mapName");
                        item.setText(1, getStatus(mapName));
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button loadButton = new Button(rightPanel, SWT.PUSH);
        loadButton.setText("Load");
        loadButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        loadButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Loading Cache",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    final Collection<String> mapNames = new ArrayList<String>();

                    for (TableItem item : items) {
                        String mapName = (String)item.getData("mapName");
                        mapNames.add(mapName);
                    }

                    IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

                    progressService.busyCursorWhile(new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor) throws InvocationTargetException {
                            try {
                                monitor.beginTask("Loading cache...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                                for (String mapName : mapNames) {

                                    if (monitor.isCanceled()) throw new InterruptedException();

                                    monitor.subTask("Loading "+mapName+"...");

                                    DN dn = getDn(mapName);

                                    moduleClient.invoke(
                                            "load",
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

                    for (TableItem item : items) {
                        String mapName = (String)item.getData("mapName");
                        item.setText(1, getStatus(mapName));
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button clearButton = new Button(rightPanel, SWT.PUSH);
        clearButton.setText("Clear");
        clearButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        clearButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Clearing Cache",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    final Collection<String> mapNames = new ArrayList<String>();

                    for (TableItem item : items) {
                        String mapName = (String)item.getData("mapName");
                        mapNames.add(mapName);
                    }

                    IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

                    progressService.busyCursorWhile(new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor) throws InvocationTargetException {
                            try {
                                monitor.beginTask("Clearing cache...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                                for (String mapName : mapNames) {

                                    if (monitor.isCanceled()) throw new InterruptedException();

                                    monitor.subTask("Clearing "+mapName+"...");

                                    DN dn = getDn(mapName);

                                    moduleClient.invoke(
                                            "clear",
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

                    for (TableItem item : items) {
                        String mapName = (String)item.getData("mapName");
                        item.setText(1, getStatus(mapName));
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button removeButton = new Button(rightPanel, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Removing Cache",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    final Collection<String> mapNames = new ArrayList<String>();

                    for (TableItem item : items) {
                        String mapName = (String)item.getData("mapName");
                        mapNames.add(mapName);
                    }

                    IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

                    progressService.busyCursorWhile(new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor) throws InvocationTargetException {
                            try {
                                monitor.beginTask("Removing cache...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                                for (String mapName : mapNames) {

                                    if (monitor.isCanceled()) throw new InterruptedException();

                                    monitor.subTask("Removing "+mapName+"...");

                                    DN dn = getDn(mapName);

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

                    for (TableItem item : items) {
                        String mapName = (String)item.getData("mapName");
                        item.setText(1, getStatus(mapName));
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        new Label(rightPanel, SWT.NONE);

        Button synchronizeButton = new Button(rightPanel, SWT.PUSH);
        synchronizeButton.setText("Synchronize");
        synchronizeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        synchronizeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Updating Cache",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    final Collection<String> mapNames = new ArrayList<String>();

                    for (TableItem item : items) {
                        String mapName = (String)item.getData("mapName");
                        mapNames.add(mapName);
                    }

                    IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

                    progressService.busyCursorWhile(new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor) throws InvocationTargetException {
                            try {
                                monitor.beginTask("Synchronizing cache...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                                for (String mapName : mapNames) {

                                    if (monitor.isCanceled()) throw new InterruptedException();

                                    monitor.subTask("Synchronizing "+mapName+"...");

                                    DN dn = getDn(mapName);

                                    moduleClient.invoke(
                                            "synchronize",
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

                    for (TableItem item : items) {
                        String mapName = (String)item.getData("mapName");
                        item.setText(1, getStatus(mapName));
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        new Label(rightPanel, SWT.NONE);
        
        Button refreshButton = new Button(rightPanel, SWT.PUSH);
        refreshButton.setText("Refresh");
        refreshButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                refresh();
            }
        });

        return composite;
    }

    public void refresh() {
        try {
            int[] indices = table.getSelectionIndices();
            TableItem[] items = table.getSelection();

            final Collection<String> mapNames = new ArrayList<String>();

            if (items.length == 0) {
                for (String mapName : NIS.mapLabels.values()) {
                    mapNames.add(mapName);
                }

                table.removeAll();

            } else {
                for (TableItem ti : items) {
                    String mapName = (String)ti.getData("mapName");
                    mapNames.add(mapName);
                }
            }

            final Map<String,String> statuses = new TreeMap<String,String>();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Refreshing...", mapNames.size() == 1 ? IProgressMonitor.UNKNOWN : mapNames.size());

                        for (String mapName : mapNames) {

                            if (monitor.isCanceled()) throw new InterruptedException();

                            monitor.subTask("Checking "+mapName+"...");

                            statuses.put(mapName, getStatus(mapName));

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

            if (items.length == 0) {
                for (String mapName : mapNames) {

                    String count = statuses.get(mapName);

                    TableItem ti = new TableItem(table, SWT.NONE);
                    ti.setText(0, mapName);
                    ti.setText(1, count == null ? "N/A" : ""+count);

                    ti.setData("mapName", mapName);
                }

            } else {
                for (TableItem ti : items) {
                    String mapName = (String)ti.getData("mapName");

                    String count = statuses.get(mapName);
                    ti.setText(1, count == null ? "N/A" : ""+count);
                }
            }

            table.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public DN getDn(String mapName) throws Exception {

        RDNBuilder rb = new RDNBuilder();
        rb.set("ou", mapName);
        RDN rdn = rb.toRdn();

        DNBuilder db = new DNBuilder();
        db.append(rdn);

        DN suffix = partitionClient.getSuffixes().iterator().next();
        db.append(suffix);

        return db.toDn();
    }

    public String getStatus(String mapName) {
        try {
            return ""+moduleClient.invoke(
                    "getCount",
                    new Object[] { getDn(mapName) },
                    new String[] { DN.class.getName() }
            );

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "N/A";
        }
    }
}
