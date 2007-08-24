package org.safehaus.penrose.studio.nis.editor;

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
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.SourceConfigs;
import org.safehaus.penrose.source.SourceSyncConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.management.PartitionClient;
import org.safehaus.penrose.management.SourceClient;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.jdbc.QueryResponse;

import java.util.*;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class NISCachePage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table table;

    NISDomainEditor editor;
    NISDomain domain;
    NISTool nisTool;

    Project project;
    PenroseClient penroseClient;
    PartitionClient partitionClient;

    Map<String,String> sourceNames = new TreeMap<String,String>();

    public NISCachePage(NISDomainEditor editor) throws Exception {
        super(editor, "CACHE", "  Cache  ");

        this.editor = editor;

        domain = editor.getDomain();
        nisTool = editor.getNisTool();
        project = nisTool.getProject();

        penroseClient = project.getClient();
        partitionClient = penroseClient.getPartitionClient(domain.getName());

        sourceNames.put("Users", "nis_users");
        sourceNames.put("Shadows", "nis_shadow");
        sourceNames.put("Hosts", "nis_hosts");
        sourceNames.put("Groups", "nis_groups");
        sourceNames.put("Services", "nis_services");
        sourceNames.put("RPCs", "nis_rpcs");
        sourceNames.put("NetIDs", "nis_netids");
        sourceNames.put("Protocols", "nis_protocols");
        sourceNames.put("Aliases", "nis_aliases");
        sourceNames.put("Netgroups", "nis_netgroups");
        sourceNames.put("Ethernets", "nis_ethers");
        sourceNames.put("BootParams", "nis_bootparams");
        sourceNames.put("Networks", "nis_networks");
        sourceNames.put("Automounts", "nis_automounts");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Cache");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Cache");
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

                    TableItem[] items = table.getSelection();

                    for (TableItem item : items) {
                        String name = (String)item.getData();

                        try {
                            SourceClient sourceClient = partitionClient.getSourceClient(name);
                            sourceClient.createCache();
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

                    TableItem[] items = table.getSelection();

                    for (TableItem item : items) {
                        String name = (String)item.getData();

                        try {
                            SourceClient sourceClient = partitionClient.getSourceClient(name);
                            sourceClient.loadCache();
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

                    TableItem[] items = table.getSelection();

                    for (TableItem item : items) {
                        String name = (String)item.getData();

                        try {
                            SourceClient sourceClient = partitionClient.getSourceClient(name);
                            sourceClient.cleanCache();
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

                    TableItem[] items = table.getSelection();

                    for (TableItem item : items) {
                        String name = (String)item.getData();

                        try {
                            SourceClient sourceClient = partitionClient.getSourceClient(name);
                            sourceClient.dropCache();
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

            Partition partition = nisTool.getNisPartition();
            Connection connection = partition.getConnection(NISTool.NIS_CONNECTION_NAME);
            JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
            JDBCClient client = adapter.getClient();

            PartitionConfigs partitionConfigs = project.getPartitionConfigs();
            PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(domain.getName());
            SourceConfigs sourceConfigs = partitionConfig.getSourceConfigs();

            for (final String label : sourceNames.keySet()) {
                final String sourceName = sourceNames.get(label);
                log.debug("Checking cache for "+label+" ("+sourceName+").");

                SourceSyncConfig sourceSyncConfig = sourceConfigs.getSourceSyncConfig(sourceName);
                if (sourceSyncConfig == null) continue;

                Collection<String> destinations = sourceSyncConfig.getDestinations();
                if (destinations.isEmpty()) continue;

                String cacheName = destinations.iterator().next();
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