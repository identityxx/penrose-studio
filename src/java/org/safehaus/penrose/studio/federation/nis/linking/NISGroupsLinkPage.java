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
import org.safehaus.penrose.federation.repository.NISDomain;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.filter.SubstringFilter;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.FilterTool;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.jdbc.connection.JDBCConnection;

import java.util.Collection;
import java.util.ArrayList;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class NISGroupsLinkPage extends FormPage {

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

    Text groupText;
    Text gidText;
    Text membersText;

    NISLinkEditor editor;
    NISDomain domain;
    NISFederation nisFederation;

    Partition partition;

    JDBCConnection jdbcConnection;

    Source localGroups;
    Source globalGroups;
    Source groupsLink;

    public NISGroupsLinkPage(NISLinkEditor editor) {
        super(editor, "GROUPS", "  Groups  ");

        this.editor = editor;
        domain = editor.getDomain();
        nisFederation = editor.getNisTool();

        partition = nisFederation.getPartitions().getPartition(domain.getName()+"_"+NISFederation.NIS);

        Connection connection = partition.getConnection(NISFederation.CACHE_CONNECTION_NAME);
        jdbcConnection = (JDBCConnection)connection;

        localGroups = partition.getSource("local_groups");
        globalGroups = partition.getSource("global_groups");
        groupsLink = partition.getSource("cache_groups_link");

        Display display = Display.getDefault();

        red = display.getSystemColor(SWT.COLOR_RED);
        green = display.getSystemColor(SWT.COLOR_GREEN);
        blue = display.getSystemColor(SWT.COLOR_BLUE);
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Groups");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section localSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        localSection.setText("Local Groups");
        localSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control localControl = createLocalControl(localSection);
        localSection.setClient(localControl);

        Section globalSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        globalSection.setText("Global Groups");
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
                    ErrorDialog.open(e);
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
                        String cn = (String)localAttributes.getValue("cn");

                        removeLink(cn);
                        item.setData("link", null);

                        Collection<SearchResult> results = getGlobal(item);

                        updateStatus(item, results);

                        if (count == 1) {
                            updateGlobal(results);
                        }
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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

        gidText = new Text(rightColumn, SWT.BORDER);
        gidText.setEnabled(false);
        gidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label membersLabel = new Label(rightColumn, SWT.NONE);
        membersLabel.setText("Members:");
        gd = new GridData();
        gd.verticalAlignment = GridData.BEGINNING;
        membersLabel.setLayoutData(gd);

        membersText = new Text(rightColumn, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        membersText.setEnabled(false);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 100;
        membersText.setLayoutData(gd);

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

                    Collection<SearchResult> results = searchGlobal(groupText.getText());
                    updateGlobal(results);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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
                        String cn = (String)attributes.getValue("cn");

                        String newLink = groupText.getText();

                        RDNBuilder rb = new RDNBuilder();
                        rb.set("cn", newLink);

                        Attributes globalAttributes = (Attributes)attributes.clone();
                        globalAttributes.setValue("cn", newLink);

                        globalGroups.add(null, rb.toRdn(), globalAttributes);

                        String link = (String)item.getData("link");
                        if (link == null) {
                            createLink(cn, newLink);

                        } else {
                            updateLink(cn, newLink);
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
                        String cn = (String)attributes.getValue("cn");

                        globalGroups.add(null, result.getDn().getRdn(), result.getAttributes());

                        String link = (String)item.getData("link");
                        if (link == null) {
                            createLink(cn, cn);

                        } else {
                            updateLink(cn, cn);
                        }

                        item.setData("link", cn);

                        Collection<SearchResult> results = getGlobal(item);
                        updateStatus(item, results);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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
                    int count = localTable.getSelectionCount();
                    if (count == 0) return;

                    if (count == 1) {
                        if (globalTable.getSelectionCount() == 0) return;

                        TableItem item = localTable.getSelection()[0];
                        TableItem globalItem = globalTable.getSelection()[0];

                        SearchResult localResult = (SearchResult)item.getData("local");
                        Attributes localAttributes = localResult.getAttributes();
                        String cn = (String)localAttributes.getValue("cn");

                        SearchResult globalResult = (SearchResult)globalItem.getData("global");
                        Attributes globalAttributes = globalResult.getAttributes();
                        String newLink = (String)globalAttributes.getValue("cn");

                        String link = (String)item.getData("link");
                        if (link == null) {
                            createLink(cn, newLink);

                        } else {
                            updateLink(cn, newLink);
                        }

                        item.setData("link", newLink);

                        Collection<SearchResult> results = getGlobal(item);

                        updateStatus(item, results);
                        updateGlobal(results);

                        return;
                    }

                    for (TableItem item : localTable.getSelection()) {
                        String cn = item.getText(0);
                        String newLink = item.getText(1);
                        String status = item.getText(2);
                        if (!POSSIBLE_MATCH.equals(status)) continue;

                        String link = (String)item.getData("link");
                        if (link == null) {
                            createLink(cn, newLink);

                        } else {
                            updateLink(cn, newLink);
                        }

                        item.setData("link", newLink);

                        Collection<SearchResult> results = getGlobal(item);

                        updateStatus(item, results);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
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
                        String globalCn = (String)rdn.get("cn");

                        globalGroups.delete(null, rdn);

                        SearchResult localResult = (SearchResult)item.getData("local");
                        Attributes localAttributes = localResult.getAttributes();
                        String cn = (String)localAttributes.getValue("cn");

                        if (cn.equals(globalCn)) {
                            removeLink(cn);
                            item.setData("link", null);
                        }

                        Collection<SearchResult> results = searchGlobal(groupText.getText());

                        updateStatus(item, results);
                        updateGlobal(results);
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);

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
        String cn = (String)attributes.getValue("cn");

        String link = (String)item.getData("link");

        if (link == null) {
            return searchGlobal(cn);

        } else {
            return findGlobal(link);
        }
    }

    public Collection<SearchResult> searchGlobal(String cn) throws Exception {

        Filter filter = null;
        filter = FilterTool.appendOrFilter(filter, createFilter("cn", cn));

        SearchRequest request = new SearchRequest();
        request.setFilter(filter);

        SearchResponse response = new SearchResponse();

        globalGroups.search(null, request, response);

        return response.getAll();
    }

    public Collection<SearchResult> findGlobal(String cn) throws Exception {

        RDNBuilder rb = new RDNBuilder();
        rb.set("cn", cn);

        SearchRequest request = new SearchRequest();
        request.setDn(rb.toRdn());

        SearchResponse response = new SearchResponse();

        try {
            globalGroups.search(null, request, response);
            return response.getAll();

        } catch (Exception e) {
            return new ArrayList<SearchResult>();
        }
    }

    public void updateLocal(TableItem item) throws Exception {
        SearchResult result = (SearchResult)item.getData("local");
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
    }

    public void clearLocal() throws Exception {
        groupText.setText("");
        gidText.setText("");
        membersText.setText("");
    }

    public void updateStatus(TableItem item, Collection<SearchResult> results) throws Exception {
        String link = (String)item.getData("link");
        if (link != null) {

            if (results.size() == 1) {

                SearchResult r = results.iterator().next();
                Attributes attrs = r.getAttributes();
                String cn = (String)attrs.getValue("cn");

                if (link.equals(cn)) {
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
            String cn = (String)attrs.getValue("cn");

            item.setText(1, cn);
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
                    String cn = (String)attributes.getValue("cn");

                    TableItem item = new TableItem(localTable, SWT.NONE);
                    item.setText(0, cn == null ? "" : cn);
                    item.setData("local", result);

                    String link = getLink(cn);
                    item.setData("link", link);

                    Collection<SearchResult> results = getGlobal(item);
                    updateStatus(item, results);
                }
            };

            localGroups.search(null, request, response);

            localTable.setSelection(selection);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public String getLink(String cn) throws Exception {
        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(cn));

        QueryResponse queryResponse = new QueryResponse() {
            public void add(Object object) throws Exception {
                ResultSet rs = (ResultSet)object;
                String globalCn = rs.getString(1);
                super.add(globalCn);
            }
        };

        jdbcConnection.executeQuery(
                "select globalCn from "+ jdbcConnection.getTableName(groupsLink)+" where cn=?",
                assignments,
                queryResponse
        );

        return queryResponse.hasNext() ? (String)queryResponse.next() : null;
    }

    public void createLink(String cn, String globalCn) throws Exception {

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(cn));
        assignments.add(new Assignment(globalCn));

        jdbcConnection.executeUpdate(
                "insert into "+ jdbcConnection.getTableName(groupsLink)+" (cn, globalCn) values (?, ?)",
                assignments
        );
    }

    public void updateLink(String cn, String globalCn) throws Exception {

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(globalCn));
        assignments.add(new Assignment(cn));

        jdbcConnection.executeUpdate(
                "update "+ jdbcConnection.getTableName(groupsLink)+" set globalCn=? where cn=?",
                assignments
        );
    }

    public void removeLink(String cn) throws Exception {

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(cn));

        jdbcConnection.executeUpdate(
                "delete from "+ jdbcConnection.getTableName(groupsLink)+" where cn=?",
                assignments
        );
    }

}
