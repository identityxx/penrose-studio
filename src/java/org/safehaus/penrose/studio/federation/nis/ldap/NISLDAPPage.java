package org.safehaus.penrose.studio.federation.nis.ldap;

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
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.NISRepository;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.partition.Partitions;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.management.PartitionClient;
import org.safehaus.penrose.management.SchedulerClient;
import org.safehaus.penrose.management.JobClient;
import org.safehaus.penrose.directory.Directory;
import org.safehaus.penrose.directory.Entry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author Endi S. Dewata
 */
public class NISLDAPPage extends FormPage {

    public DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISLDAPEditor editor;
    NISFederation nisFederation;
    NISRepository domain;

    Table table;

    public NISLDAPPage(NISLDAPEditor editor) {
        super(editor, "LDAP", "  LDAP  ");

        this.editor = editor;
        this.nisFederation = editor.getNisTool();
        this.domain = editor.getDomain();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("LDAP");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section ldapSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        ldapSection.setText("LDAP");
        ldapSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control ldapControl = createLDAPControl(ldapSection);
        ldapSection.setClient(ldapControl);

        refreshLDAP();
    }

    public Composite createLDAPControl(Composite parent) {

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

        Button createBaseButton = new Button(rightPanel, SWT.PUSH);
        createBaseButton.setText("Create Base");
        createBaseButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createBaseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Creating base LDAP entry",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    Project project = nisFederation.getProject();
                    PenroseClient client = project.getClient();
                    PartitionClient partitionClient = client.getPartitionClient(domain.getName());
                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();
                    JobClient jobClient = schedulerClient.getJobClient("LDAPSync");

                    Partitions partitions = nisFederation.getPartitions();
                    Partition partition = partitions.getPartition(domain.getName());

                    Directory directory = partition.getDirectory();
                    Entry suffix = directory.getRootEntries().iterator().next();

                    jobClient.invoke(
                            "create",
                            new Object[] { suffix.getDn() },
                            new String[] { DN.class.getName() }
                    );

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        Button removeBaseButton = new Button(rightPanel, SWT.PUSH);
        removeBaseButton.setText("Remove Base");
        removeBaseButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeBaseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Removing base LDAP entry",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    Project project = nisFederation.getProject();
                    PenroseClient client = project.getClient();
                    PartitionClient partitionClient = client.getPartitionClient(domain.getName());
                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();
                    JobClient jobClient = schedulerClient.getJobClient("LDAPSync");

                    Partitions partitions = nisFederation.getPartitions();
                    Partition partition = partitions.getPartition(domain.getName());

                    Directory directory = partition.getDirectory();
                    Entry suffix = directory.getRootEntries().iterator().next();

                    jobClient.invoke(
                            "remove",
                            new Object[] { suffix.getDn() },
                            new String[] { DN.class.getName() }
                    );

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        new Label(rightPanel, SWT.NONE);

        Button createButton = new Button(rightPanel, SWT.PUSH);
        createButton.setText("Create");
        createButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Creating LDAP Subtree",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    Project project = nisFederation.getProject();
                    PenroseClient client = project.getClient();
                    PartitionClient partitionClient = client.getPartitionClient(domain.getName());
                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();
                    JobClient jobClient = schedulerClient.getJobClient("LDAPSync");

                    Partitions partitions = nisFederation.getPartitions();
                    Partition partition = partitions.getPartition(domain.getName());

                    Source ldap = partition.getSource("LDAP");

                    for (TableItem ti : items) {
                        Entry entry = (Entry)ti.getData();

                        try {
                            jobClient.invoke(
                                    "create",
                                    new Object[] { entry.getDn().toString() },
                                    new String[] { String.class.getName() }
                            );
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }

                        Long count = getCount(ldap, entry.getDn());
                        ti.setText(1, count == null ? "N/A" : ""+count);
                    }

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
                            "Loading LDAP Subtree",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    Project project = nisFederation.getProject();
                    PenroseClient client = project.getClient();
                    PartitionClient partitionClient = client.getPartitionClient(domain.getName());
                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();
                    JobClient jobClient = schedulerClient.getJobClient("LDAPSync");

                    Partitions partitions = nisFederation.getPartitions();
                    Partition partition = partitions.getPartition(domain.getName());
                    Source ldap = partition.getSource("LDAP");

                    for (TableItem ti : items) {
                        Entry entry = (Entry)ti.getData();

                        try {
                            jobClient.invoke(
                                    "load",
                                    new Object[] { entry.getDn().toString() },
                                    new String[] { String.class.getName() }
                            );
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }

                        Long count = getCount(ldap, entry.getDn());
                        ti.setText(1, count == null ? "N/A" : ""+count);
                    }

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
                            "Clearing LDAP Subtree",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    Project project = nisFederation.getProject();
                    PenroseClient client = project.getClient();
                    PartitionClient partitionClient = client.getPartitionClient(domain.getName());
                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();
                    JobClient jobClient = schedulerClient.getJobClient("LDAPSync");

                    Partitions partitions = nisFederation.getPartitions();
                    Partition partition = partitions.getPartition(domain.getName());
                    Source ldap = partition.getSource("LDAP");

                    for (TableItem ti : items) {
                        Entry entry = (Entry)ti.getData();

                        try {
                            jobClient.invoke(
                                    "clear",
                                    new Object[] { entry.getDn().toString() },
                                    new String[] { String.class.getName() }
                            );
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }

                        Long count = getCount(ldap, entry.getDn());
                        ti.setText(1, count == null ? "N/A" : ""+count);
                    }

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
                            "Removing LDAP Subtree",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    Project project = nisFederation.getProject();
                    PenroseClient client = project.getClient();
                    PartitionClient partitionClient = client.getPartitionClient(domain.getName());
                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();
                    JobClient jobClient = schedulerClient.getJobClient("LDAPSync");

                    Partitions partitions = nisFederation.getPartitions();
                    Partition partition = partitions.getPartition(domain.getName());
                    Source ldap = partition.getSource("LDAP");

                    for (TableItem ti : items) {
                        Entry entry = (Entry)ti.getData();

                        try {
                            jobClient.invoke(
                                    "remove",
                                    new Object[] { entry.getDn().toString() },
                                    new String[] { String.class.getName() }
                            );
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }

                        Long count = getCount(ldap, entry.getDn());
                        ti.setText(1, count == null ? "N/A" : ""+count);
                    }

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
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Synchronize LDAP",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    Project project = nisFederation.getProject();
                    PenroseClient client = project.getClient();
                    PartitionClient partitionClient = client.getPartitionClient(domain.getName());
                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();

                    JobClient jobClient = schedulerClient.getJobClient("LDAPSync");
                    jobClient.invoke("synchronize", new Object[] {}, new String[] {});

                    refreshLDAP();
                    
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
                refreshLDAP();
            }
        });

        return composite;
    }

    public void refreshLDAP() {
        try {
            int[] indices = table.getSelectionIndices();

            table.removeAll();

            Partitions partitions = nisFederation.getPartitions();
            Partition partition = partitions.getPartition(domain.getName());
            Source ldap = partition.getSource("LDAP");

            Directory directory = partition.getDirectory();
            Entry suffix = directory.getRootEntries().iterator().next();

            for (String sourceName : nisFederation.getSourceNames()) {
                String sourceLabel = nisFederation.getSourceLabel(sourceName);

                RDNBuilder rb = new RDNBuilder();
                rb.set("ou", sourceLabel);
                RDN rdn = rb.toRdn();

                DNBuilder db = new DNBuilder();
                db.append(rdn);
                db.append(suffix.getDn());

                DN dn = db.toDn();

                Long count = getCount(ldap, dn);

                TableItem ti = new TableItem(table, SWT.NONE);
                ti.setText(0, sourceLabel);
                ti.setText(1, count == null ? "N/A" : ""+count);

                Entry entry = suffix.getChildren(rdn).iterator().next();
                ti.setData(entry);
            }

            table.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
        }
    }

    public Long getCount(Source source, DN baseDn) {
        try {
            SearchRequest request = new SearchRequest();
            request.setDn(baseDn);
            request.setScope(SearchRequest.SCOPE_ONE);
            request.setAttributes(new String[] { "dn" });
            request.setTypesOnly(true);

            SearchResponse response = new SearchResponse();

            source.search(request, response);

            return response.getTotalCount();

        } catch (Exception e) {
            return null;
        }
    }
}
