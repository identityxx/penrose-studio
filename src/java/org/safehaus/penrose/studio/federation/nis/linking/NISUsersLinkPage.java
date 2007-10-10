package org.safehaus.penrose.studio.federation.nis.linking;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.NISRepository;
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
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class NISUsersLinkPage extends FormPage {

    public Logger log = Logger.getLogger(getClass());

    public final static String LINKED           = "Linked";
    public final static String MISSING_LINK     = "Missing Link";
    public final static String NOT_FOUND        = "Not Found";
    public final static String POSSIBLE_MATCH   = "Possible Match";
    public final static String MULTIPLE_RESULTS = "Multiple Results";

    FormToolkit toolkit;

    Color red;
    Color green;
    Color blue;

    Table localTable;
    Table globalTable;

    Text userText;
    Text nameText;
    Text uidText;
    Text gidText;
    Text homeText;
    Text shellText;

    NISLinkEditor editor;
    NISRepository domain;
    NISFederation nisFederation;

    Partition partition;

    JDBCClient client;

    Source localUsers;
    Source globalUsers;
    Source usersLink;

    public NISUsersLinkPage(NISLinkEditor editor) {
        super(editor, "USERS", "  Users  ");

        this.editor = editor;
        domain = editor.getDomain();
        nisFederation = editor.getNisTool();

        partition = nisFederation.getPartitions().getPartition(domain.getName());

        Connection connection = partition.getConnection(NISFederation.CACHE_CONNECTION_NAME);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        client = adapter.getClient();

        localUsers = partition.getSource("local_users");
        globalUsers = partition.getSource("global_users");
        usersLink = partition.getSource("cache_users_link");

        Display display = Display.getDefault();

        red = display.getSystemColor(SWT.COLOR_RED);
        green = display.getSystemColor(SWT.COLOR_GREEN);
        blue = display.getSystemColor(SWT.COLOR_BLUE);
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Users");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section localSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        localSection.setText("Local Users");
        localSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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

        localTable = new Table(leftColumn, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd = new GridData();
        gd.heightHint = 200;
        localTable.setLayoutData(gd);

        localTable.setHeaderVisible(true);
        localTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(localTable, SWT.NONE);
        tc.setText("Local");
        tc.setWidth(100);

        tc = new TableColumn(localTable, SWT.NONE);
        tc.setText("Global");
        tc.setWidth(100);

        tc = new TableColumn(localTable, SWT.NONE);
        tc.setText("Status");
        tc.setWidth(100);

        localTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (localTable.getSelectionCount() != 1) {
                        clearLocal();
                        clearGlobal();
                        return;
                    }

                    TableItem item = localTable.getSelection()[0];

                    Collection<SearchResult> results = getGlobal(item);
                    updateStatus(item, results);

                    updateLocal(item);
                    updateGlobal(results);

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
                    int count = localTable.getSelectionCount();
                    if (count == 0) return;

                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Unlink",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    for (TableItem item : localTable.getSelection()) {
                        SearchResult localResult = (SearchResult)item.getData("local");
                        Attributes localAttributes = localResult.getAttributes();
                        String uid = (String)localAttributes.getValue("uid");

                        removeLink(uid);
                        item.setData("link", null);

                        Collection<SearchResult> results = getGlobal(item);
                        updateStatus(item, results);

                        if (count == 1) {
                            updateGlobal(results);
                        }
                    }

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
        gd = new GridData();
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

        uidText = new Text(rightColumn, SWT.BORDER);
        uidText.setEnabled(false);
        uidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label gidNumberLabel = new Label(rightColumn, SWT.NONE);
        gidNumberLabel.setText("GID:");
        gidNumberLabel.setLayoutData(new GridData());

        gidText = new Text(rightColumn, SWT.BORDER);
        gidText.setEnabled(false);
        gidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label homeDirectoryLabel = new Label(rightColumn, SWT.NONE);
        homeDirectoryLabel.setText("Home:");
        homeDirectoryLabel.setLayoutData(new GridData());

        homeText = new Text(rightColumn, SWT.BORDER);
        homeText.setEnabled(false);
        homeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label loginShellLabel = new Label(rightColumn, SWT.NONE);
        loginShellLabel.setText("Shell:");
        loginShellLabel.setLayoutData(new GridData());

        shellText = new Text(rightColumn, SWT.BORDER);
        shellText.setEnabled(false);
        shellText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(rightColumn, SWT.NONE);

        Composite rightButtons = toolkit.createComposite(rightColumn);
        rightButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rightButtons.setLayout(new RowLayout());

        Button searchButton = new Button(rightButtons, SWT.PUSH);
        searchButton.setText("  Search  ");

        searchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (localTable.getSelectionCount() == 0) return;

                    Collection<SearchResult> results = searchGlobal(userText.getText(), nameText.getText());
                    updateGlobal(results);

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
                    int count = localTable.getSelectionCount();
                    if (count == 0) return;

                    if (count == 1) {
                        TableItem item = localTable.getSelection()[0];

                        SearchResult result = (SearchResult)item.getData("local");
                        Attributes attributes = result.getAttributes();
                        String uid = (String)attributes.getValue("uid");

                        String newLink = userText.getText();

                        RDNBuilder rb = new RDNBuilder();
                        rb.set("uid", newLink);

                        Attributes globalAttributes = (Attributes)attributes.clone();
                        globalAttributes.setValue("uid", newLink);
                        globalAttributes.setValue("cn", nameText.getText());

                        globalUsers.add(rb.toRdn(), globalAttributes);

                        String link = (String)item.getData("link");
                        if (link == null) {
                            createLink(uid, newLink);

                        } else {
                            updateLink(uid, newLink);
                        }

                        item.setData("link", newLink);

                        Collection<SearchResult> results = getGlobal(item);

                        updateStatus(item, results);
                        updateGlobal(results);

                        return;
                    }

                    for (TableItem item : localTable.getSelection()) {
                        String status = item.getText(2);
                        if (!NOT_FOUND.equals(status)) continue;
                        
                        SearchResult result = (SearchResult)item.getData("local");
                        Attributes attributes = result.getAttributes();
                        String uid = (String)attributes.getValue("uid");

                        globalUsers.add(result.getDn().getRdn(), result.getAttributes());

                        String link = (String)item.getData("link");
                        if (link == null) {
                            createLink(uid, uid);

                        } else {
                            updateLink(uid, uid);
                        }

                        item.setData("link", uid);

                        Collection<SearchResult> results = getGlobal(item);
                        updateStatus(item, results);
                    }

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
                    int count = localTable.getSelectionCount();
                    if (count == 0) return;

                    if (count == 1) {
                        if (globalTable.getSelectionCount() == 0) return;

                        TableItem item = localTable.getSelection()[0];
                        TableItem globalItem = globalTable.getSelection()[0];

                        SearchResult localResult = (SearchResult)item.getData("local");
                        Attributes localAttributes = localResult.getAttributes();
                        String uid = (String)localAttributes.getValue("uid");

                        SearchResult globalResult = (SearchResult)globalItem.getData("global");
                        Attributes globalAttributes = globalResult.getAttributes();
                        String newLink = (String)globalAttributes.getValue("uid");

                        String link = (String)item.getData("link");
                        if (link == null) {
                            createLink(uid, newLink);

                        } else {
                            updateLink(uid, newLink);
                        }

                        item.setData("link", newLink);

                        Collection<SearchResult> results = getGlobal(item);

                        updateStatus(item, results);
                        updateGlobal(results);

                        return;
                    }

                    for (TableItem item : localTable.getSelection()) {
                        String uid = item.getText(0);
                        String newLink = item.getText(1);
                        String status = item.getText(2);
                        if (!POSSIBLE_MATCH.equals(status)) continue;

                        String link = (String)item.getData("link");
                        if (link == null) {
                            createLink(uid, newLink);

                        } else {
                            updateLink(uid, newLink);
                        }

                        item.setData("link", newLink);

                        Collection<SearchResult> results = getGlobal(item);

                        updateStatus(item, results);
                    }

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
                    if (localTable.getSelectionCount() == 0) return;
                    if (globalTable.getSelectionCount() == 0) return;

                    TableItem item = localTable.getSelection()[0];

                    for (TableItem globalItem : globalTable.getSelection()) {
                        SearchResult result = (SearchResult)globalItem.getData("global");

                        boolean confirm = MessageDialog.openQuestion(
                                editor.getSite().getShell(),
                                "Delete",
                                "Are you sure?"
                        );

                        if (!confirm) return;

                        RDN rdn = result.getDn().getRdn();
                        String globalUid = (String)rdn.get("uid");

                        globalUsers.delete(rdn);

                        SearchResult localResult = (SearchResult)item.getData("local");
                        Attributes localAttributes = localResult.getAttributes();
                        String uid = (String)localAttributes.getValue("uid");

                        if (uid.equals(globalUid)) {
                            removeLink(uid);
                            item.setData("link", null);
                        }

                        Collection<SearchResult> results = searchGlobal(userText.getText(), nameText.getText());

                        updateStatus(item, results);
                        updateGlobal(results);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());

                }
            }
        });

        return composite;
    }

    public Filter createFilter(String name, String s) {
        if (s == null || "".equals(s)) return null;

        Collection<Object> substrings = new ArrayList<Object>();
        substrings.add(SubstringFilter.STAR);

        StringBuilder sb = null;

        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) {
                if (sb == null) sb = new StringBuilder();
                sb.append(c);

            } else if (sb != null) {
                if (sb.length() >= 2) {
                    substrings.add(sb.toString());
                    substrings.add(SubstringFilter.STAR);
                }
                sb = null;
            }
        }

        if (sb != null) {
            substrings.add(sb.toString());
            substrings.add(SubstringFilter.STAR);
        }

        return new SubstringFilter(name, substrings);
    }

    public void updateGlobal(Collection<SearchResult> results) throws Exception {

        globalTable.removeAll();

        for (SearchResult result : results) {
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
            ti.setData("global", result);
        }

        globalTable.setSelection(0);
    }

    public void clearGlobal() {
        globalTable.removeAll();
    }

    public Collection<SearchResult> getGlobal(TableItem item) throws Exception {
        SearchResult result = (SearchResult)item.getData("local");
        Attributes attributes = result.getAttributes();
        String uid = (String)attributes.getValue("uid");
        String cn = (String)attributes.getValue("cn");

        String link = (String)item.getData("link");

        if (link == null) {
            return searchGlobal(uid, cn);

        } else {
            return findGlobal(link);
        }
    }

    public Collection<SearchResult> searchGlobal(String uid, String cn) throws Exception {

        Filter filter = null;
        filter = FilterTool.appendOrFilter(filter, createFilter("uid", uid));
        filter = FilterTool.appendOrFilter(filter, createFilter("cn", cn));

        SearchRequest request = new SearchRequest();
        request.setFilter(filter);

        SearchResponse response = new SearchResponse();

        globalUsers.search(request, response);

        return response.getAll();
    }

    public Collection<SearchResult> findGlobal(String uid) throws Exception {

        RDNBuilder rb = new RDNBuilder();
        rb.set("uid", uid);

        SearchRequest request = new SearchRequest();
        request.setDn(rb.toRdn());

        SearchResponse response = new SearchResponse();

        try {
            globalUsers.search(request, response);
            return response.getAll();

        } catch (Exception e) {
            return new ArrayList<SearchResult>();
        }
    }

    public void updateLocal(TableItem item) throws Exception {
        SearchResult result = (SearchResult)item.getData("local");
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
    }

    public void clearLocal() throws Exception {
        userText.setText("");
        nameText.setText("");
        uidText.setText("");
        gidText.setText("");
        homeText.setText("");
        shellText.setText("");
    }

    public void updateStatus(TableItem item, Collection<SearchResult> results) throws Exception {
        String link = (String)item.getData("link");
        if (link != null) {

            if (results.size() == 1) {

                SearchResult r = results.iterator().next();
                Attributes attrs = r.getAttributes();
                String uid = (String)attrs.getValue("uid");

                if (link.equals(uid)) {
                    item.setText(1, link);
                    item.setText(2, LINKED);
                    item.setForeground(2, green);
                    return;
                }
            }

            item.setText(1, link);
            item.setText(2, MISSING_LINK);
            item.setForeground(2, red);

        } else if (results.size() > 1) {
            item.setText(1, "");
            item.setText(2, MULTIPLE_RESULTS);
            item.setForeground(2, red);

        } else if (results.size() == 1) {
            SearchResult r = results.iterator().next();
            Attributes attrs = r.getAttributes();
            String uid = (String)attrs.getValue("uid");

            item.setText(1, uid);
            item.setText(2, POSSIBLE_MATCH);
            item.setForeground(2, blue);

        } else {
            item.setText(1, "");
            item.setText(2, NOT_FOUND);
            item.setForeground(2, red);
        }
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

                    TableItem item = new TableItem(localTable, SWT.NONE);
                    item.setText(0, uid == null ? "" : uid);
                    item.setData("local", result);

                    String link = getLink(uid);
                    item.setData("link", link);

                    Collection<SearchResult> results = getGlobal(item);
                    updateStatus(item, results);
                }
            };

            localUsers.search(request, response);

            localTable.setSelection(selection);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            MessageDialog.openError(editor.getSite().getShell(), "Action Failed", e.getMessage());
        }
    }

    public String getLink(String uid) throws Exception {
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

        return queryResponse.hasNext() ? (String)queryResponse.next() : null;
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

    public void removeLink(String uid) throws Exception {

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(uid));

        client.executeUpdate(
                "delete from "+client.getTableName(usersLink)+" where uid=?",
                assignments
        );
    }

}
