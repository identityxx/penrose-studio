package org.safehaus.penrose.studio.federation.nis.database;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.federation.nis.NISDomain;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.SourceConfigs;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.management.*;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.scheduler.SchedulerConfig;
import org.safehaus.penrose.scheduler.JobConfig;

import java.util.*;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class NISDatabaseCachePage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table table;

    NISDatabaseEditor editor;
    NISDomain domain;
    NISFederation nisFederation;

    Project project;
    PenroseClient penroseClient;
    PartitionClient partitionClient;

    Map<String,Collection<String>> sourceCaches = new TreeMap<String,Collection<String>>();

    public NISDatabaseCachePage(NISDatabaseEditor editor) throws Exception {
        super(editor, "DATABASE", "  Database  ");

        this.editor = editor;

        domain = editor.getDomain();
        nisFederation = editor.getNisTool();
        project = nisFederation.getProject();

        penroseClient = project.getClient();
        partitionClient = penroseClient.getPartitionClient(domain.getName());

        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(domain.getName());
        SchedulerConfig schedulerConfig = partitionConfig.getSchedulerConfig();

        log.debug("Source caches:");
        for (String sourceName : nisFederation.getSourceNames()) {

            JobConfig jobConfig = schedulerConfig.getJobConfig(sourceName);
            if (jobConfig == null) continue;

            String target = jobConfig.getParameter("target");

            StringTokenizer st = new StringTokenizer(target, ";, ");

            Collection<String> list = new ArrayList<String>();
            while (st.hasMoreTokens()) list.add(st.nextToken());

            log.debug(" - "+sourceName+": "+list);
            
            sourceCaches.put(sourceName, list);
        }
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
    }

    public void setActive(boolean b) {
        super.setActive(b);
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

                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();

                    for (TableItem item : table.getSelection()) {
                        String sourceName = (String)item.getData();

                        try {
                            JobClient jobClient = schedulerClient.getJobClient(sourceName);
                            jobClient.invoke("create", new Object[] {}, new String[] {});

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
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

                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();

                    for (TableItem item : table.getSelection()) {
                        String sourceName = (String)item.getData();

                        try {
                            JobClient jobClient = schedulerClient.getJobClient(sourceName);
                            jobClient.invoke("load", new Object[] {}, new String[] {});

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
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

                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();

                    for (TableItem item : table.getSelection()) {
                        String sourceName = (String)item.getData();

                        try {
                            JobClient jobClient = schedulerClient.getJobClient(sourceName);
                            jobClient.invoke("clear", new Object[] {}, new String[] {});

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
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

                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();

                    for (TableItem item : table.getSelection()) {
                        String sourceName = (String)item.getData();

                        try {
                            JobClient jobClient = schedulerClient.getJobClient(sourceName);
                            jobClient.invoke("drop", new Object[] {}, new String[] {});

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
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

                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();

                    for (TableItem item : table.getSelection()) {
                        String sourceName = (String)item.getData();

                        try {
                            JobClient jobClient = schedulerClient.getJobClient(sourceName);
                            jobClient.invoke("synchronize", new Object[] {}, new String[] {});

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
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

            table.removeAll();

            Partition partition = nisFederation.getPartition();
            Connection connection = partition.getConnection(Federation.JDBC);
            JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
            JDBCClient client = adapter.getClient();

            PartitionConfigs partitionConfigs = project.getPartitionConfigs();
            PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(domain.getName());
            SchedulerConfig schedulerConfig = partitionConfig.getSchedulerConfig();
            SourceConfigs sourceConfigs = partitionConfig.getSourceConfigs();

            for (String sourceName : nisFederation.getSourceNames()) {
                String label = nisFederation.getSourceLabel(sourceName);
                log.debug("Checking cache for "+label+" ("+sourceName+").");

                Collection<String> caches = sourceCaches.get(sourceName);
                if (caches == null) continue;

                String cacheName = caches.iterator().next();
                SourceConfig sourceConfig = sourceConfigs.getSourceConfig(cacheName);

                final TableItem ti = new TableItem(table, SWT.NONE);
                ti.setText(0, label);
                ti.setData(sourceName);

                try {
                    String table = client.getTableName(sourceConfig);
                    String sql = "select count(*) from "+table;

                    Collection<Assignment> assignments = new ArrayList<Assignment>();

                    QueryResponse queryResponse = new QueryResponse() {
                        public void add(Object object) throws Exception {
                            ResultSet rs = (ResultSet)object;
                            Integer count = rs.getInt(1);
                            ti.setText(1, count.toString());
                        }
                    };

                    client.executeQuery(sql, assignments, queryResponse);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ti.setText(1, "N/A");
                }
            }

            table.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
        }
    }

}
