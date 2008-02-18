package org.safehaus.penrose.studio.federation.nis.editor;

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
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.federation.repository.NISDomain;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.Partitions;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.management.*;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.jdbc.connection.JDBCConnection;

import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author Endi S. Dewata
 */
public class NISLDAPPage extends FormPage {

    public DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Project project;
    NISEditor editor;
    NISFederation nisFederation;

    Table table;

    public NISLDAPPage(NISEditor editor, NISFederation nisFederation) {
        super(editor, "LDAP", "  LDAP  ");

        this.editor = editor;
        this.project = editor.getProject();
        this.nisFederation = nisFederation;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("LDAP");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("LDAP");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control sourcesSection = createPartitionsSection(section);
        section.setClient(sourcesSection);

        refresh();
    }

    public Composite createPartitionsSection(Composite parent) {

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
        tc.setWidth(100);
        tc.setText("Status");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(150);
        tc.setText("Last Change Number");

        tc = new TableColumn(table, SWT.NONE);
        tc.setWidth(150);
        tc.setText("Last Updated");

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

        Button loadButton = new Button(rightPanel, SWT.PUSH);
        loadButton.setText("Load");
        loadButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        loadButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Creating LDAP",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    PenroseClient penroseClient = project.getClient();

                    for (TableItem ti : items) {
                        try {
                            NISDomain domain = (NISDomain)ti.getData();

                            PartitionClient partitionClient = penroseClient.getPartitionClient(domain.getName()+"_"+NISFederation.YP);

                            SourceClient penrose = partitionClient.getSourceClient("Penrose");
                            SourceClient ldap = partitionClient.getSourceClient("LDAP");

                            SearchRequest request = new SearchRequest();
                            SearchResponse response = new SearchResponse();

                            penrose.search(request, response);

                            while (response.hasNext()) {
                                SearchResult result = response.next();

                                log.debug("Adding "+result.getDn());

                                try {
                                    ldap.add(result.getDn(), result.getAttributes());
                                } catch (Exception e) {
                                    log.error(e.getMessage());
                                }
                            }

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }

                refresh();
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
                            "Removing LDAP",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    PenroseClient penroseClient = project.getClient();

                    for (TableItem ti : items) {
                        try {
                            NISDomain domain = (NISDomain)ti.getData();

                            PartitionClient partitionClient = penroseClient.getPartitionClient(domain.getName()+"_"+NISFederation.YP);

                            ArrayList<DN> dns = new ArrayList<DN>();
                            SourceClient ldap = partitionClient.getSourceClient("LDAP");

                            SearchRequest request = new SearchRequest();
                            SearchResponse response = new SearchResponse();

                            ldap.search(request, response);

                            while (response.hasNext()) {
                                SearchResult result = response.next();
                                dns.add(result.getDn());
                            }

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
                    ErrorDialog.open(e);
                }

                refresh();
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
                            "Creating LDAP",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem[] items = table.getSelection();

                    PenroseClient client = nisFederation.getProject().getClient();

                    for (TableItem ti : items) {
                        NISDomain domain = (NISDomain)ti.getData();

                        PartitionClient partitionClient = client.getPartitionClient(domain.getName()+"_"+NISFederation.YP);
                        SchedulerClient schedulerClient = partitionClient.getSchedulerClient();
                        JobClient jobClient = schedulerClient.getJobClient("LDAPSync");
                        jobClient.invoke("synchronize", new Object[] {}, new String[] {});
                    }

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }

                refresh();
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

            PenroseClient penroseClient = project.getClient();

            for (NISDomain domain : nisFederation.getRepositories()) {
                PartitionClient partitionClient = penroseClient.getPartitionClient(domain.getName()+"_"+NISFederation.YP);
                SourceClient ldap = partitionClient.getSourceClient("LDAP");

                boolean exists;

                try {
                    SearchRequest request = new SearchRequest();
                    request.setScope(SearchRequest.SCOPE_BASE);
                    
                    SearchResponse response = new SearchResponse();

                    ldap.search(request, response);

                    exists = response.hasNext();
                    
                } catch (Exception e) {
                    exists = false;
                }

                TableItem ti = new TableItem(table, SWT.NONE);

                ti.setText(0, domain.getName());
                ti.setText(1, exists ? "OK" : "Missing");
/*
                SourceClient tracker = partitionClient.getSourceClient("tracker");
                String trackerTableName = tracker.getParameter("table");

                ConnectionClient connectionClient = partitionClient.getConnectionClient(tracker.getConnectionName());
                //String trackerTableName = connectionClient.getTableName(tracker);

                QueryResponse response = new QueryResponse();

                connectionClient.invoke(
                        "executeQuery",
                        new Object[] {
                                "select max(changeNumber), max(changeTimestamp) from "+trackerTableName,
                                response
                        },
                        new String[] { }
                );

                String lastChangeNumber;
                String lastChangeTimestamp;

                if (response.hasNext()) {
                    Object object = response.next();
                    ResultSet rs = (ResultSet)object;

                    Object[] objects = new Object[] { rs.getObject(1), rs.getTimestamp(2) };
                    lastChangeNumber = objects[0] == null ? "" : ""+objects[0];
                    lastChangeTimestamp = objects[1] == null ? "" : df.format((Timestamp)objects[1]);

                } else {
                    lastChangeNumber = "";
                    lastChangeTimestamp = "";
                }

                ti.setText(2, lastChangeNumber);
                ti.setText(3, lastChangeTimestamp);
*/
                ti.setData(domain);
            }

            table.select(indices);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

}
