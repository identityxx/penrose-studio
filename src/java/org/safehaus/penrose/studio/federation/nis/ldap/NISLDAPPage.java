package org.safehaus.penrose.studio.federation.nis.ldap;

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
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.NISDomain;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.management.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Endi S. Dewata
 */
public class NISLDAPPage extends FormPage {

    public DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Project project;
    NISLDAPEditor editor;
    NISFederation nisFederation;
    NISDomain domain;

    Table table;

    PartitionClient partitionClient;
    DN suffix;
    ModuleClient moduleClient;

    public NISLDAPPage(NISLDAPEditor editor) throws Exception {
        super(editor, "Content", "  Content  ");

        this.editor = editor;
        this.project = editor.getProject();
        this.nisFederation = editor.getNisTool();
        this.domain = editor.getDomain();

        PenroseClient penroseClient = project.getClient();

        partitionClient = penroseClient.getPartitionClient(domain.getName());
        
        suffix = partitionClient.getSuffixes().iterator().next();
        moduleClient = partitionClient.getModuleClient("NISLDAPSyncModule");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS LDAP Server");

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

        refresh();
    }

    public Composite createBaseSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftPanel = toolkit.createComposite(composite);
        leftPanel.setLayout(new GridLayout(2, false));
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label suffixLabel = toolkit.createLabel(leftPanel, "Base DN:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        suffixLabel.setLayoutData(gd);

        Label suffixText = toolkit.createLabel(leftPanel, domain.getSuffix());
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite rightPanel = toolkit.createComposite(composite);
        rightPanel.setLayout(new GridLayout());
        gd = new GridData(GridData.FILL_VERTICAL);
        gd.verticalSpan = 2;
        gd.widthHint = 120;
        rightPanel.setLayoutData(gd);

        Button createBaseButton = new Button(rightPanel, SWT.PUSH);
        createBaseButton.setText("Create Base");
        createBaseButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createBaseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                createBase();
            }
        });

        Button removeBaseButton = new Button(rightPanel, SWT.PUSH);
        removeBaseButton.setText("Remove Base");
        removeBaseButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeBaseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                removeBase();
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
        gd.verticalSpan = 2;
        gd.widthHint = 120;
        rightPanel.setLayoutData(gd);

        Button createButton = new Button(rightPanel, SWT.PUSH);
        createButton.setText("Create");
        createButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                create();
            }
        });
/*
        Button loadButton = new Button(rightPanel, SWT.PUSH);
        loadButton.setText("Load");
        loadButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        loadButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                load();
            }
        });
*/
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

    public void createBase() {
        try {
            boolean confirm = MessageDialog.openQuestion(
                    editor.getSite().getShell(),
                    "Creating base LDAP entry",
                    "Are you sure?"
            );

            if (!confirm) return;

            moduleClient.invoke(
                    "createBase",
                    new Object[] { },
                    new String[] { }
            );

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void removeBase() {
        try {
            boolean confirm = MessageDialog.openQuestion(
                    editor.getSite().getShell(),
                    "Removing base LDAP entry",
                    "Are you sure?"
            );

            if (!confirm) return;

            moduleClient.invoke(
                    "removeBase",
                    new Object[] { },
                    new String[] { }
            );

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void create() {
        try {
            if (table.getSelectionCount() == 0) return;

            boolean confirm = MessageDialog.openQuestion(
                    editor.getSite().getShell(),
                    "Creating LDAP Subtree",
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
                        monitor.beginTask("Creating subtree...", mapNames.size());

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

            Map<String,String> statuses = refresh(mapNames);

            for (TableItem ti : items) {
                String mapName = (String)ti.getData("mapName");

                String status = statuses.get(mapName);
                ti.setText(1, status);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void load() {
        try {
            if (table.getSelectionCount() == 0) return;

            boolean confirm = MessageDialog.openQuestion(
                    editor.getSite().getShell(),
                    "Loading LDAP Subtree",
                    "Are you sure?"
            );

            if (!confirm) return;

            TableItem[] items = table.getSelection();

            final Collection<String> mapNames = new ArrayList<String>();

            for (TableItem ti : items) {
                String mapName = (String)ti.getData("mapName");
                mapNames.add(mapName);
            }

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Loading LDAP...", mapNames.size());

                        for (String mapName : mapNames) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            DN dn = getDn(mapName);
                            monitor.subTask("Loading "+dn+"...");

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

            Map<String,String> statuses = refresh(mapNames);

            for (TableItem ti : items) {
                String mapName = (String)ti.getData("mapName");

                String status = statuses.get(mapName);
                ti.setText(1, status);
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
                    "Clearing LDAP Subtree",
                    "Are you sure?"
            );

            if (!confirm) return;

            TableItem[] items = table.getSelection();

            final Collection<String> mapNames = new ArrayList<String>();

            for (TableItem ti : items) {
                String mapName = (String)ti.getData("mapName");
                mapNames.add(mapName);
            }

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Clearing LDAP...", mapNames.size());

                        for (String mapName : mapNames) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            DN dn = getDn(mapName);

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

            Map<String,String> statuses = refresh(mapNames);

            for (TableItem ti : items) {
                String mapName = (String)ti.getData("mapName");

                String status = statuses.get(mapName);
                ti.setText(1, status);
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
                    "Removing LDAP Subtree",
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
                        monitor.beginTask("Removing subtree...", mapNames.size());

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

            Map<String,String> statuses = refresh(mapNames);

            for (TableItem ti : items) {
                String mapName = (String)ti.getData("mapName");

                String status = statuses.get(mapName);
                ti.setText(1, status);
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
                    "Synchronize LDAP",
                    "Are you sure?"
            );

            if (!confirm) return;

            TableItem[] items = table.getSelection();

            final Collection<String> mapNames = new ArrayList<String>();
            for (TableItem ti : items) {
                String mapName = (String)ti.getData("mapName");
                mapNames.add(mapName);
            }

            final Collection<Boolean> errors = new ArrayList<Boolean>();

            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

            progressService.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        monitor.beginTask("Synchronizing LDAP...", mapNames.size());

                        for (String mapName : mapNames) {
                            if (monitor.isCanceled()) throw new InterruptedException();

                            DN dn = getDn(mapName);
                            monitor.subTask("Synchronizing "+dn+"...");

                            Boolean status = (Boolean)moduleClient.invoke(
                                    "synchronize",
                                    new Object[] { dn },
                                    new String[] { DN.class.getName() }
                            );

                            if (!status) errors.add(status);

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

            Map<String,String> statuses = refresh(mapNames);

            for (TableItem ti : items) {
                String mapName = (String)ti.getData("mapName");

                String status = statuses.get(mapName);
                ti.setText(1, status);
            }

            if (!errors.isEmpty()) {
                ErrorDialog.open("ERROR", "Error(s) occured during synchronization. See Errors tab.");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void refresh() {
        try {
            int[] indices = table.getSelectionIndices();
            TableItem[] items = table.getSelection();

            final Collection<String> mapNames = new ArrayList<String>();

            if (items.length == 0) {
                for (String mapName : nisFederation.getMapNames()) {
                    mapNames.add(mapName);
                }

                table.removeAll();

            } else {
                for (TableItem ti : items) {
                    String mapName = (String)ti.getData("mapName");
                    mapNames.add(mapName);
                }
            }

            Map<String,String> statuses = refresh(mapNames);

            if (items.length == 0) {
                for (String mapName : mapNames) {

                    String status = statuses.get(mapName);

                    TableItem ti = new TableItem(table, SWT.NONE);
                    ti.setText(0, mapName);
                    ti.setText(1, status);

                    ti.setData("mapName", mapName);
                }

            } else {
                for (TableItem ti : items) {
                    String mapName = (String)ti.getData("mapName");

                    String status = statuses.get(mapName);
                    ti.setText(1, status);
                }
            }

            table.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public Map<String,String> refresh(final Collection<String> mapNames) throws Exception {

        final Map<String,String> statuses = new TreeMap<String,String>();

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        progressService.busyCursorWhile(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    monitor.beginTask("Refreshing...", nisFederation.getMapNames().size());

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

        return statuses;
    }

    public DN getDn(String mapName) throws Exception {
        RDNBuilder rb = new RDNBuilder();
        rb.set("ou", mapName);
        RDN rdn = rb.toRdn();

        DNBuilder db = new DNBuilder();
        db.append(rdn);
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
