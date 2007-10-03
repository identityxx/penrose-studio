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
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.FilterTool;
import org.safehaus.penrose.filter.SubstringFilter;

import java.util.Collection;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class NISUsersLinkPage extends FormPage {

    public Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table localTable;
    Table globalTable;

    Text userText;
    Text nameText;
    Text uidText;
    Text gidText;
    Text homeText;
    Text shellText;

    NISLinkEditor editor;
    NISDomain domain;
    NISTool nisTool;

    Partition partition;

    JDBCClient client;

    Source localUsers;
    Source globalUsers;
    Source usersLink;

    public NISUsersLinkPage(NISLinkEditor editor) {
        super(editor, "USERS", "  Users  ");

        this.editor = editor;
        domain = editor.getDomain();
        nisTool = editor.getNisTool();

        partition = nisTool.getPartitions().getPartition(domain.getName());

        Connection connection = partition.getConnection(NISTool.CACHE_CONNECTION_NAME);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        client = adapter.getClient();

        localUsers = partition.getSource("local_users");
        globalUsers = partition.getSource("global_users");
        usersLink = partition.getSource("cache_users_link");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Users");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section localSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        localSection.setText("Local Users");
        localSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control localControl = createLocalControl(localSection);
        localSection.setClient(localControl);

        Section globalSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        globalSection.setText("Global Users");
        globalSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control globalControl = createGlobalControl(globalSection);
        globalSection.setClient(globalControl);

        refresh();
    }

    public Composite createLocalControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftColumn = toolkit.createComposite(composite);
        leftColumn.setLayoutData(new GridData(GridData.FILL_VERTICAL));
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
                    String uid = (String)attributes.getValue("uid");
                    String cn = (String)attributes.getValue("cn");
                    String uidNumber = (String)attributes.getValue("uidNumber");
                    String gidNumber = (String)attributes.getValue("gidNumber");
                    String homeDirectory = (String)attributes.getValue("homeDirectory");
                    String loginShell = (String)attributes.getValue("loginShell");

                    userText.setText(uid == null ? "" : uid);
                    nameText.setText(cn == null ? "" : cn);
                    uidText.setText(uidNumber == null ? "" : uidNumber);
                    gidText.setText(gidNumber == null ? "" : gidNumber);
                    homeText.setText(homeDirectory == null ? "" : homeDirectory);
                    shellText.setText(loginShell == null ? "" : loginShell);

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
                    String localUid = (String)localAttributes.getValue("uid");

                    unlink(localUid);

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

        Label uidLabel = new Label(rightColumn, SWT.NONE);
        uidLabel.setText("User:");
        GridData gd = new GridData();
        gd.widthHint = 80;
        uidLabel.setLayoutData(gd);

        userText = new Text(rightColumn, SWT.BORDER);
        userText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label nameLabel = new Label(rightColumn, SWT.NONE);
        nameLabel.setText("Name:");
        nameLabel.setLayoutData(new GridData());

        nameText = new Text(rightColumn, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label uidNumberLabel = new Label(rightColumn, SWT.NONE);
        uidNumberLabel.setText("UID:");
        uidNumberLabel.setLayoutData(new GridData());

        uidText = new Text(rightColumn, SWT.BORDER | SWT.READ_ONLY);
        uidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label gidNumberLabel = new Label(rightColumn, SWT.NONE);
        gidNumberLabel.setText("GID:");
        gidNumberLabel.setLayoutData(new GridData());

        gidText = new Text(rightColumn, SWT.BORDER | SWT.READ_ONLY);
        gidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label homeDirectoryLabel = new Label(rightColumn, SWT.NONE);
        homeDirectoryLabel.setText("Home:");
        homeDirectoryLabel.setLayoutData(new GridData());

        homeText = new Text(rightColumn, SWT.BORDER | SWT.READ_ONLY);
        homeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label loginShellLabel = new Label(rightColumn, SWT.NONE);
        loginShellLabel.setText("Shell:");
        loginShellLabel.setLayoutData(new GridData());

        shellText = new Text(rightColumn, SWT.BORDER | SWT.READ_ONLY);
        shellText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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
                    rb.set("uid", userText.getText());

                    Attributes attributes = (Attributes)result.getAttributes().clone();
                    attributes.setValue("uid", userText.getText());
                    attributes.setValue("cn", nameText.getText());

                    globalUsers.add(rb.toRdn(), attributes);

                    search();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
                }
            }
        });

        return composite;
    }

    public Composite createGlobalControl(Composite parent) {

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
        tc.setText("User");
        tc.setWidth(100);

        tc = new TableColumn(globalTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(200);

        tc = new TableColumn(globalTable, SWT.NONE);
        tc.setText("UID");
        tc.setWidth(60);

        tc = new TableColumn(globalTable, SWT.NONE);
        tc.setText("GID");
        tc.setWidth(60);

        tc = new TableColumn(globalTable, SWT.NONE);
        tc.setText("Home");
        tc.setWidth(150);

        tc = new TableColumn(globalTable, SWT.NONE);
        tc.setText("Shell");
        tc.setWidth(100);

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
                    String localUid = (String)localAttributes.getValue("uid");
                    String link = localItem.getText(1);

                    if (globalTable.getSelectionCount() == 0) return;

                    TableItem globalItem = globalTable.getSelection()[0];
                    SearchResult globalResult = (SearchResult)globalItem.getData();
                    Attributes globalAttributes = globalResult.getAttributes();
                    String globalUid = (String)globalAttributes.getValue("uid");

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Link",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    if ("".equals(link)) {
                        createLink(localUid, globalUid);
                    } else {
                        updateLink(localUid, globalUid);
                    }

                    localItem.setText(1, globalUid);
                    
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

                    globalUsers.delete(result.getDn().getRdn());

                    search();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());

                }
            }
        });

        return composite;
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

        if (! "".equals(userText.getText())) {
            filter = FilterTool.appendAndFilter(filter, createFilter("uid", userText.getText()));
        }

        if (! "".equals(nameText.getText())) {
            filter = FilterTool.appendAndFilter(filter, createFilter("cn", nameText.getText()));
        }

        SearchRequest request = new SearchRequest();
        request.setFilter(filter);

        SearchResponse response = new SearchResponse() {
            public void add(SearchResult result) throws Exception {
                Attributes attributes = result.getAttributes();
                String globalUid = (String)attributes.getValue("uid");
                String globalCn = (String)attributes.getValue("cn");
                String globalUidNumber = (String)attributes.getValue("uidNumber");
                String globalGidNumber = (String)attributes.getValue("gidNumber");
                String globalHomeDirectory = (String)attributes.getValue("homeDirectory");
                String globalLoginShell = (String)attributes.getValue("loginShell");

                TableItem ti = new TableItem(globalTable, SWT.NONE);
                ti.setText(0, globalUid == null ? "" : globalUid);
                ti.setText(1, globalCn == null ? "" : globalCn);
                ti.setText(2, globalUidNumber == null ? "" : globalUidNumber);
                ti.setText(3, globalGidNumber == null ? "" : globalGidNumber);
                ti.setText(4, globalHomeDirectory == null ? "" : globalHomeDirectory);
                ti.setText(5, globalLoginShell == null ? "" : globalLoginShell);
                ti.setData(result);
            }
        };

        globalUsers.search(request, response);
    }

    public void refresh() {
        try {
            int[] selection = localTable.getSelectionIndices();
            localTable.removeAll();

            SearchRequest request = new SearchRequest();
            SearchResponse response = new SearchResponse() {
                public void add(SearchResult result) throws Exception {
                    Attributes attributes = result.getAttributes();
                    String uid = (String)attributes.getValue("uid");

                    Collection<Assignment> assignments = new ArrayList<Assignment>();
                    assignments.add(new Assignment(uid));

                    QueryResponse queryResponse = new QueryResponse() {
                        public void add(Object object) throws Exception {
                            ResultSet rs = (ResultSet)object;
                            String globalUid = rs.getString(1);
                            super.add(globalUid);
                        }
                    };

                    client.executeQuery(
                            "select globalUid from "+client.getTableName(usersLink)+" where uid=?",
                            assignments,
                            queryResponse
                    );

                    String globalUid = queryResponse.hasNext() ? (String)queryResponse.next() : null;

                    TableItem ti = new TableItem(localTable, SWT.NONE);
                    ti.setText(0, uid == null ? "" : uid);
                    ti.setText(1, globalUid == null ? "" : globalUid);
                    ti.setData(result);
                }
            };

            localUsers.search(request, response);

            localTable.setSelection(selection);
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
        }
    }

    public void createLink(String uid, String globalUid) throws Exception {

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(uid));
        assignments.add(new Assignment(globalUid));

        client.executeUpdate(
                "insert into "+client.getTableName(usersLink)+" (uid, globalUid) values (?, ?)",
                assignments
        );
    }

    public void updateLink(String uid, String globalUid) throws Exception {

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(globalUid));
        assignments.add(new Assignment(uid));

        client.executeUpdate(
                "update "+client.getTableName(usersLink)+" set globalUid=? where uid=?",
                assignments
        );
    }

    public void unlink(String uid) throws Exception {

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(uid));

        client.executeUpdate(
                "delete from "+client.getTableName(usersLink)+" where uid=?",
                assignments
        );
    }

}
