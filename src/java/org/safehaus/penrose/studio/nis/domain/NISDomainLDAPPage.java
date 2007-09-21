package org.safehaus.penrose.studio.nis.domain;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.partition.Partitions;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.management.PartitionClient;
import org.safehaus.penrose.management.SchedulerClient;
import org.safehaus.penrose.management.JobClient;
import org.safehaus.penrose.directory.Directory;
import org.safehaus.penrose.directory.Entry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Timestamp;

/**
 * @author Endi S. Dewata
 */
public class NISDomainLDAPPage extends FormPage {

    public DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISDomainEditor editor;
    NISTool nisTool;
    NISDomain domain;

    Table ldapTable;
    Table trackerTable;

    public NISDomainLDAPPage(NISDomainEditor editor) {
        super(editor, "LDAP", "  LDAP  ");

        this.editor = editor;
        this.nisTool = editor.getNisTool();
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

        Section trackerSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        trackerSection.setText("Change Log Tracker");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 300;
        trackerSection.setLayoutData(gd);

        Control trackerControl = createTrackerControl(trackerSection);
        trackerSection.setClient(trackerControl);

        refreshLDAP();
        refreshTracker();
    }

    public Composite createLDAPControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftPanel = toolkit.createComposite(composite);
        leftPanel.setLayout(new GridLayout());
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        ldapTable = new Table(leftPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        ldapTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        ldapTable.setHeaderVisible(true);
        ldapTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(ldapTable, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Name");

        tc = new TableColumn(ldapTable, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Status");

        Composite rightPanel = toolkit.createComposite(composite);
        rightPanel.setLayout(new GridLayout());
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.verticalSpan = 2;
        gd.widthHint = 120;
        rightPanel.setLayoutData(gd);

        Button loadButton = new Button(rightPanel, SWT.PUSH);
        loadButton.setText("Load");
        loadButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        loadButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (ldapTable.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Creating LDAP Entries",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = ldapTable.getSelection();

                    Partitions partitions = nisTool.getPartitions();
                    Partition partition = partitions.getPartition(domain.getName());
                    final Source penrose = partition.getSource("Penrose");
                    final Source ldap = partition.getSource("LDAP");

                    for (TableItem ti : items) {
                        Entry entry = (Entry)ti.getData();

                        try {
                            SearchRequest request = new SearchRequest();
                            request.setDn(entry.getDn().getRdn());

                            SearchResponse response = new SearchResponse() {
                                public void add(SearchResult result) throws Exception {

                                    log.debug("Adding "+result.getDn());

                                    try {
                                        ldap.add(result.getDn(), result.getAttributes());
                                    } catch (Exception e) {
                                        log.error(e.getMessage());
                                    }
                                }
                            };

                            penrose.search(request, response);

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }

                refreshLDAP();
            }
        });

        Button removeButton = new Button(rightPanel, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (ldapTable.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Removing LDAP Entries",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = ldapTable.getSelection();

                    Partitions partitions = nisTool.getPartitions();
                    Partition partition = partitions.getPartition(domain.getName());
                    final Source ldap = partition.getSource("LDAP");

                    for (TableItem ti : items) {
                        Entry entry = (Entry)ti.getData();

                        final ArrayList<DN> dns = new ArrayList<DN>();

                        try {
                            SearchRequest request = new SearchRequest();
                            request.setDn(entry.getDn().getRdn());
                            
                            SearchResponse response = new SearchResponse() {
                                public void add(SearchResult result) throws Exception {
                                    dns.add(result.getDn());
                                }
                            };

                            ldap.search(request, response);

                            for (int i=dns.size()-1; i>=0; i--) {
                                DN dn = dns.get(i);
                                ldap.delete(dn);
                            }

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }

                refreshLDAP();
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

    public Composite createTrackerControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftPanel = toolkit.createComposite(composite);
        leftPanel.setLayout(new GridLayout());
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        trackerTable = new Table(leftPanel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        trackerTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        trackerTable.setHeaderVisible(true);
        trackerTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(trackerTable, SWT.NONE);
        tc.setWidth(100);
        tc.setText("Change Number");

        tc = new TableColumn(trackerTable, SWT.NONE);
        tc.setWidth(300);
        tc.setText("Change Time");

        Composite rightPanel = toolkit.createComposite(composite);
        rightPanel.setLayout(new GridLayout());
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.verticalSpan = 2;
        gd.widthHint = 120;
        rightPanel.setLayoutData(gd);

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

                    Project project = nisTool.getProject();
                    PenroseClient client = project.getClient();
                    PartitionClient partitionClient = client.getPartitionClient(domain.getName());
                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();
                    
                    JobClient jobClient = schedulerClient.getJobClient("LDAPSync");
                    jobClient.invoke("synchronize", new Object[] {}, new String[] {});

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }

                refreshTracker();
            }
        });

        Button removeButton = new Button(rightPanel, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (trackerTable.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Removing Tracker",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = trackerTable.getSelection();

                    //Partitions partitions = nisTool.getPartitions();
                    //Partition partition = partitions.getPartition(domain.getName());
                    //Source tracker = partition.getSource("tracker");

                    Project project = nisTool.getProject();
                    PenroseClient client = project.getClient();
                    PartitionClient partitionClient = client.getPartitionClient(domain.getName());
                    SchedulerClient schedulerClient = partitionClient.getSchedulerClient();

                    JobClient jobClient = schedulerClient.getJobClient("LDAPSync");

                    for (TableItem ti : items) {
                        SearchResult searchResult = (SearchResult)ti.getData();
                        Attributes attributes = searchResult.getAttributes();
                        Long changeNumber = Long.parseLong(attributes.getValue("changeNumber").toString());

                        try {
                            jobClient.invoke("removeTracker", new Object[] { changeNumber }, new String[] { Long.class.getName() });
                            //tracker.delete(searchResult.getDn());

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }

                refreshTracker();
            }
        });

        new Label(rightPanel, SWT.NONE);

        Button refreshButton = new Button(rightPanel, SWT.PUSH);
        refreshButton.setText("Refresh");
        refreshButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                refreshTracker();
            }
        });

        return composite;
    }

    public void refreshLDAP() {
        try {
            int[] indices = ldapTable.getSelectionIndices();

            ldapTable.removeAll();

            Partitions partitions = nisTool.getPartitions();
            Partition partition = partitions.getPartition(domain.getName());
            Source ldap = partition.getSource("LDAP");

            Directory directory = partition.getDirectory();
            Entry entry = directory.getRootEntries().iterator().next();

            for (Entry child : entry.getChildren()) {

                boolean exists;

                try {
                    SearchRequest request = new SearchRequest();
                    request.setDn(child.getDn().getRdn());
                    request.setScope(SearchRequest.SCOPE_BASE);

                    SearchResponse response = new SearchResponse();

                    ldap.search(request, response);

                    exists = response.hasNext();

                } catch (Exception e) {
                    exists = false;
                }

                TableItem ti = new TableItem(ldapTable, SWT.NONE);

                DN dn = child.getDn();
                RDN rdn = dn.getRdn();
                String label = (String)rdn.get("ou");

                ti.setText(0, label);
                ti.setText(1, exists ? "OK" : "Missing");

                ti.setData(child);
            }

            ldapTable.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
        }
    }

    public void refreshTracker() {
        try {
            int[] indices = trackerTable.getSelectionIndices();

            trackerTable.removeAll();

            Partitions partitions = nisTool.getPartitions();
            Partition partition = partitions.getPartition(domain.getName());
            Source tracker = partition.getSource("tracker");


            SearchRequest request = new SearchRequest();
            SearchResponse response = new SearchResponse() {
                public void add(SearchResult result) {

                    Attributes attributes = result.getAttributes();
                    String changeNumber = attributes.getValue("changeNumber").toString();
                    String changeTimestamp = df.format((Timestamp)attributes.getValue("changeTimestamp"));

                    TableItem ti = new TableItem(trackerTable, SWT.NONE);

                    ti.setText(0, changeNumber);
                    ti.setText(1, changeTimestamp);

                    ti.setData(result);
                }
            };

            tracker.search(request, response);

            trackerTable.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
        }
    }
}
