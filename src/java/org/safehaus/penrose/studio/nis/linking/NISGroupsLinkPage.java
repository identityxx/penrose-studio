package org.safehaus.penrose.studio.nis.linking;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
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
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.filter.SubstringFilter;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.FilterTool;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.jdbc.QueryResponse;

import java.util.Collection;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class NISGroupsLinkPage extends FormPage {

    public Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table localTable;
    Table globalTable;

    Text groupText;
    Text gidText;
    Text membersText;

    NISLinkEditor editor;
    NISDomain domain;
    NISTool nisTool;

    Partition partition;

    Source localGroups;
    Source globalGroups;
    Source groupsLink;

    public NISGroupsLinkPage(NISLinkEditor editor) {
        super(editor, "GROUPS", "  Groups  ");

        this.editor = editor;
        domain = editor.getDomain();
        nisTool = editor.getNisTool();

        partition = nisTool.getPartitions().getPartition(domain.getName());

        localGroups = partition.getSource("local_groups");
        globalGroups = partition.getSource("global_groups");
        groupsLink = partition.getSource("cache_groups_link");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Groups");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section localGroupsSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        localGroupsSection.setText("Local Groups");
        localGroupsSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control localGroupsControl = createLocalGroupsControl(localGroupsSection);
        localGroupsSection.setClient(localGroupsControl);

        Section globalGroupsSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        globalGroupsSection.setText("Global Groups");
        globalGroupsSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control globalGroupsControl = createGlobalGroupsControl(globalGroupsSection);
        globalGroupsSection.setClient(globalGroupsControl);

        refresh();
    }

    public Composite createLocalGroupsControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftColumn = toolkit.createComposite(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 250;
        leftColumn.setLayoutData(gd);
        leftColumn.setLayout(new GridLayout());

        localTable = new Table(leftColumn, SWT.BORDER | SWT.FULL_SELECTION);
        localTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        localTable.setHeaderVisible(true);
        localTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(localTable, SWT.NONE);
        tc.setText("Local");
        tc.setWidth(100);

        tc = new TableColumn(localTable, SWT.NONE);
        tc.setText("Global");
        tc.setWidth(100);

        localTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (localTable.getSelectionCount() == 0) return;

                    globalTable.removeAll();

                    TableItem item = localTable.getSelection()[0];

                    SearchResult result = (SearchResult)item.getData();
                    Attributes attributes = result.getAttributes();
                    String cn = (String)attributes.getValue("cn");
                    String gidNumber = (String)attributes.getValue("gidNumber");

                    Collection<Object> memberUids = attributes.getValues("memberUid");
                    StringBuilder sb = new StringBuilder();
                    for (Object o : memberUids) {
                        String memberUid = (String)o;
                        sb.append(memberUid);
                        sb.append("\n");
                    }

                    groupText.setText(cn == null ? "" : cn);
                    gidText.setText(gidNumber == null ? "" : gidNumber);
                    membersText.setText(sb.toString());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        Composite leftButtons = toolkit.createComposite(leftColumn);
        leftButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        leftButtons.setLayout(new RowLayout());

        Button refreshButton = new Button(leftButtons, SWT.PUSH);
        refreshButton.setText("  Refresh  ");

        refreshButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                refresh();
            }
        });

        Button unlinkButton = new Button(leftButtons, SWT.PUSH);
        unlinkButton.setText("  Unlink  ");

        unlinkButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (localTable.getSelectionCount() == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Unlink",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    TableItem localItem = localTable.getSelection()[0];
                    SearchResult localResult = (SearchResult)localItem.getData();
                    Attributes localAttributes = localResult.getAttributes();
                    String localCn = (String)localAttributes.getValue("cn");

                    unlink(localCn);

                    localItem.setText(1, "");

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        Composite rightColumn = toolkit.createComposite(composite);
        rightColumn.setLayoutData(new GridData(GridData.FILL_BOTH));
        rightColumn.setLayout(new GridLayout(2, false));

        Label groupLabel = new Label(rightColumn, SWT.NONE);
        groupLabel.setText("Group:");
        gd = new GridData();
        gd.widthHint = 80;
        groupLabel.setLayoutData(gd);

        groupText = new Text(rightColumn, SWT.BORDER);
        groupText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label gidNumberLabel = new Label(rightColumn, SWT.NONE);
        gidNumberLabel.setText("GID:");
        gidNumberLabel.setLayoutData(new GridData());

        gidText = new Text(rightColumn, SWT.BORDER | SWT.READ_ONLY);
        gidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label membersLabel = new Label(rightColumn, SWT.NONE);
        membersLabel.setText("Members:");
        gd = new GridData();
        gd.verticalAlignment = GridData.BEGINNING;
        membersLabel.setLayoutData(gd);

        membersText = new Text(rightColumn, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 100;
        membersText.setLayoutData(gd);

        new Label(rightColumn, SWT.NONE);

        Composite rightButtons = toolkit.createComposite(rightColumn);
        rightButtons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        rightButtons.setLayout(new RowLayout());

        Button searchButton = new Button(rightButtons, SWT.PUSH);
        searchButton.setText("  Search  ");

        searchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    search();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        Button createButton = new Button(rightButtons, SWT.PUSH);
        createButton.setText("  Create  ");

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (localTable.getSelectionCount() == 0) return;

                    TableItem ti = localTable.getSelection()[0];
                    SearchResult result = (SearchResult)ti.getData();

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Create",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    RDNBuilder rb = new RDNBuilder();
                    rb.set("cn", groupText.getText());

                    Attributes attributes = (Attributes)result.getAttributes().clone();
                    attributes.setValue("cn", groupText.getText());

                    globalGroups.add(rb.toRdn(), attributes);

                    search();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        return composite;
    }

    public Composite createGlobalGroupsControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        globalTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        globalTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        globalTable.setHeaderVisible(true);
        globalTable.setLinesVisible(true);

        globalTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (globalTable.getSelectionCount() == 0) return;

                    TableItem item = globalTable.getSelection()[0];

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        TableColumn tc = new TableColumn(globalTable, SWT.NONE);
        tc.setText("Group");
        tc.setWidth(150);

        tc = new TableColumn(globalTable, SWT.NONE);
        tc.setText("GID");
        tc.setWidth(80);

        tc = new TableColumn(globalTable, SWT.NONE);
        tc.setText("Members");
        tc.setWidth(400);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttons.setLayout(new RowLayout());

        Button linkButton = new Button(buttons, SWT.PUSH);
        linkButton.setText("  Link  ");

        linkButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (localTable.getSelectionCount() == 0) return;

                    TableItem localItem = localTable.getSelection()[0];
                    SearchResult localResult = (SearchResult)localItem.getData();
                    Attributes localAttributes = localResult.getAttributes();
                    String localCn = (String)localAttributes.getValue("cn");
                    String link = localItem.getText(1);

                    if (globalTable.getSelectionCount() == 0) return;

                    TableItem globalItem = globalTable.getSelection()[0];
                    SearchResult globalResult = (SearchResult)globalItem.getData();
                    Attributes globalAttributes = globalResult.getAttributes();
                    String globalCn = (String)globalAttributes.getValue("cn");

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Link",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    if ("".equals(link)) {
                        createLink(localCn, globalCn);
                    } else {
                        updateLink(localCn, globalCn);
                    }

                    localItem.setText(1, globalCn);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        Button deleteButton = new Button(buttons, SWT.PUSH);
        deleteButton.setText("  Delete  ");

        deleteButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (globalTable.getSelectionCount() == 0) return;

                    TableItem ti = globalTable.getSelection()[0];
                    SearchResult result = (SearchResult)ti.getData();

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Delete",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    globalGroups.delete(result.getDn().getRdn());

                    search();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());

                }
            }
        });

        return composite;
    }

    public void setActive(boolean b) {
        if (b) refresh();
        super.setActive(b);
    }

    public SubstringFilter createFilter(String name, String s) {
        Collection<Object> substrings = new ArrayList<Object>();
        substrings.add(SubstringFilter.STAR);

        StringTokenizer st = new StringTokenizer(s, " ");
        while (st.hasMoreTokens()) {
            substrings.add(st.nextToken());
            substrings.add(SubstringFilter.STAR);
        }

        return new SubstringFilter(name, substrings);
    }

    public void search() throws Exception {
        globalTable.removeAll();

        if (localTable.getSelectionCount() == 0) return;

        Filter filter = null;

        if (! "".equals(groupText.getText())) {
            filter = FilterTool.appendAndFilter(filter, createFilter("cn", groupText.getText()));
        }

        SearchRequest request = new SearchRequest();
        request.setFilter(filter);

        SearchResponse response = new SearchResponse() {
            public void add(SearchResult result) throws Exception {
                Attributes attributes = result.getAttributes();
                String globalCn = (String)attributes.getValue("cn");
                String globalGidNumber = (String)attributes.getValue("gidNumber");

                Collection<Object> memberUids = attributes.getValues("memberUid");
                StringBuilder sb = new StringBuilder();
                for (Object o : memberUids) {
                    String memberUid = (String)o;
                    if (sb.length() > 0) sb.append(",");
                    sb.append(memberUid);
                }

                TableItem ti = new TableItem(globalTable, SWT.NONE);
                ti.setText(0, globalCn == null ? "" : globalCn);
                ti.setText(1, globalGidNumber == null ? "" : globalGidNumber);
                ti.setText(2, sb.toString());
                ti.setData(result);
            }
        };

        globalGroups.search(request, response);
    }

    public void refresh() {
        try {
            int[] selection = localTable.getSelectionIndices();
            localTable.removeAll();

            Connection connection = partition.getConnection(NISTool.CACHE_CONNECTION_NAME);
            JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
            final JDBCClient client = adapter.getClient();

            SearchRequest request = new SearchRequest();
            SearchResponse response = new SearchResponse() {
                public void add(SearchResult result) throws Exception {
                    Attributes attributes = result.getAttributes();
                    String cn = (String)attributes.getValue("cn");

                    Collection<Assignment> assignments = new ArrayList<Assignment>();
                    assignments.add(new Assignment(cn));

                    QueryResponse queryResponse = new QueryResponse() {
                        public void add(Object object) throws Exception {
                            ResultSet rs = (ResultSet)object;
                            String globalCn = rs.getString(1);
                            super.add(globalCn);
                        }
                    };

                    client.executeQuery(
                            "select globalCn from "+client.getTableName(groupsLink)+" where cn=?",
                            assignments,
                            queryResponse
                    );

                    String globalCn = queryResponse.hasNext() ? (String)queryResponse.next() : null;

                    TableItem ti = new TableItem(localTable, SWT.NONE);
                    ti.setText(0, cn == null ? "" : cn);
                    ti.setText(1, globalCn == null ? "" : globalCn);
                    ti.setData(result);
                }
            };

            localGroups.search(request, response);

            localTable.setSelection(selection);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
        }
    }

    public void createLink(String cn, String globalCn) throws Exception {

        Connection connection = partition.getConnection(NISTool.CACHE_CONNECTION_NAME);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        final JDBCClient client = adapter.getClient();

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(cn));
        assignments.add(new Assignment(globalCn));

        client.executeUpdate(
                "insert into "+client.getTableName(groupsLink)+" (cn, globalCn) values (?, ?)",
                assignments
        );
    }

    public void updateLink(String cn, String globalCn) throws Exception {

        Connection connection = partition.getConnection(NISTool.CACHE_CONNECTION_NAME);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        final JDBCClient client = adapter.getClient();

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(globalCn));
        assignments.add(new Assignment(cn));

        client.executeUpdate(
                "update "+client.getTableName(groupsLink)+" set globalCn=? where cn=?",
                assignments
        );
    }

    public void unlink(String cn) throws Exception {

        Connection connection = partition.getConnection(NISTool.CACHE_CONNECTION_NAME);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        final JDBCClient client = adapter.getClient();

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(cn));

        client.executeUpdate(
                "delete from "+client.getTableName(groupsLink)+" where cn=?",
                assignments
        );
    }

}
