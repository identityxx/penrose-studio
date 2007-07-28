package org.safehaus.penrose.studio.nis;

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
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.nis.action.NISAction;
import org.safehaus.penrose.studio.nis.action.NISActionRequest;
import org.safehaus.penrose.studio.nis.action.NISActionResponse;
import org.safehaus.penrose.studio.nis.action.Conflict;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.Partitions;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class NISGroupsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Combo actionCombo;

    Label messageLabel;
    Table groupsTable;
    Table conflictsTable;
    Table matchesTable;

    NISEditor editor;
    NISDomain domain;

    Source actions;
    Source domains;

    Map<String,Collection<Conflict>> conflicts = new TreeMap<String,Collection<Conflict>>();

    public NISGroupsPage(NISEditor editor) {
        super(editor, "GROUPS", "  Groups  ");

        this.editor = editor;
        this.domain = editor.getDomain();

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        Partitions partitions = penroseApplication.getPartitions();
        Partition partition = partitions.getPartition("DEFAULT");

        actions = partition.getSource("penrose.actions");
        domains = partition.getSource("penrose.domains");
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Groups");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Action");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control sourcesSection = createActionSection(section);
        section.setClient(sourcesSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Results");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control resultsSection = createResultsSection(section);
        section.setClient(resultsSection);

        init();
    }

    public void init() {
        try {
            actionCombo.removeAll();

            SearchRequest request = new SearchRequest();
            request.setFilter("(type=groups)");

            SearchResponse<SearchResult> response = new SearchResponse<SearchResult>() {
                public void add(SearchResult result) throws Exception {
                    Attributes attributes = result.getAttributes();
                    String actionName = (String) attributes.getValue("name");
                    String className = (String) attributes.getValue("className");

                    actionCombo.add(actionName);
                    actionCombo.setData(actionName, className);
                }
            };

            actions.search(request, response);

            actionCombo.select(0);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            String message = e.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }
            MessageDialog.openError(editor.getSite().getShell(), "Init Failed", message);
        }
    }

    public Composite createActionSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(3, false));

        Label actionLabel = toolkit.createLabel(composite, "Action:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        actionLabel.setLayoutData(gd);

        actionCombo = new Combo(composite, SWT.READ_ONLY);
        actionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button runButton = new Button(composite, SWT.PUSH);
        runButton.setText("Run");
        gd = new GridData();
        gd.horizontalAlignment = GridData.END;
        gd.widthHint = 80;
        runButton.setLayoutData(gd);

        runButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    run();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", message);
                }
            }
        });

        return composite;
    }

    public Composite createResultsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout(2, false));

        messageLabel = toolkit.createLabel(composite, "");
        messageLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label conflictsLabel = toolkit.createLabel(composite, "Conflicts:");
        conflictsLabel.setLayoutData(new GridData());

        groupsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.verticalSpan = 3;
        groupsTable.setLayoutData(gd);

        groupsTable.setHeaderVisible(true);
        groupsTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(groupsTable, SWT.NONE);
        tc.setText("Group");
        tc.setWidth(100);

        tc = new TableColumn(groupsTable, SWT.NONE);
        tc.setText("GID");
        tc.setWidth(80);

        groupsTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (groupsTable.getSelectionCount() == 0) return;

                    TableItem item = groupsTable.getSelection()[0];

                    Attributes attributes = (Attributes)item.getData();

                    showConflicts(attributes);
                    showMatches(attributes);

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Edit Failed", message);
                }
            }
        });

        conflictsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        conflictsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        conflictsTable.setHeaderVisible(true);
        conflictsTable.setLinesVisible(true);

        tc = new TableColumn(conflictsTable, SWT.NONE);
        tc.setText("Domain");
        tc.setWidth(120);

        tc = new TableColumn(conflictsTable, SWT.NONE);
        tc.setText("Group");
        tc.setWidth(100);

        tc = new TableColumn(conflictsTable, SWT.NONE);
        tc.setText("GID");
        tc.setWidth(80);

        Label matchesLabel = toolkit.createLabel(composite, "Matches:");
        matchesLabel.setLayoutData(new GridData());

        matchesTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        matchesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        matchesTable.setHeaderVisible(true);
        matchesTable.setLinesVisible(true);

        tc = new TableColumn(matchesTable, SWT.NONE);
        tc.setText("Domain");
        tc.setWidth(120);

        tc = new TableColumn(matchesTable, SWT.NONE);
        tc.setText("Group");
        tc.setWidth(100);

        tc = new TableColumn(matchesTable, SWT.NONE);
        tc.setText("GID");
        tc.setWidth(80);

        Button editButton = new Button(composite, SWT.PUSH);
        editButton.setText("Edit");
        gd = new GridData();
        gd.widthHint = 80;
        editButton.setLayoutData(gd);

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (groupsTable.getSelectionCount() == 0) return;

                    TableItem item = groupsTable.getSelection()[0];

                    Attributes attributes = (Attributes)item.getData();
                    String domain = (String)attributes.getValue("domain");
                    String partition = (String)attributes.getValue("partition");
                    String cn = (String)attributes.getValue("cn");
                    Integer origGidNumber = (Integer)attributes.getValue("origGidNumber");

                    edit(domain, partition, cn, origGidNumber);

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Edit Failed", message);
                }
            }
        });

        return composite;
    }

    public void showConflicts(Attributes attributes) throws Exception {

        conflictsTable.removeAll();

        String cn = (String) attributes.getValue("cn");
        Collection<Conflict> list = conflicts.get(cn);

        if (list == null) return;

        for (Conflict conflict : list) {

            Attributes attributes2 = conflict.getAttributes2();

            String domain2 = (String) attributes2.getValue("domain");
            String cn2 = (String) attributes2.getValue("cn");
            Integer gidNumber2 = (Integer) attributes2.getValue("gidNumber");
            if (gidNumber2 == null) gidNumber2 = (Integer) attributes2.getValue("origGidNumber");

            TableItem ti = new TableItem(conflictsTable, SWT.NONE);
            ti.setText(0, domain2);
            ti.setText(1, "" + cn2);
            ti.setText(2, "" + gidNumber2);
        }
    }

    public void showMatches(Attributes attributes) throws Exception {

        matchesTable.removeAll();

        String cn = (String) attributes.getValue("cn");
        Integer gidNumber = (Integer) attributes.getValue("gidNumber");
        if (gidNumber == null) gidNumber = (Integer) attributes.getValue("origGidNumber");

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        Partitions partitions = penroseApplication.getPartitions();

        SearchRequest searchRequest = new SearchRequest();
        SearchResponse<SearchResult> searchResponse = new SearchResponse<SearchResult>();

        domains.search(searchRequest, searchResponse);

        while (searchResponse.hasNext()) {
            SearchResult searchResults = searchResponse.next();
            Attributes attrs = searchResults.getAttributes();

            final String domainName = (String) attrs.getValue("name");
            String partitionName = (String) attrs.getValue("partition");
            Partition partition = partitions.getPartition(partitionName);

            if (domain.getName().equals(domainName)) continue;

            Source users = partition.getSource("cache.groups");

            JDBCAdapter adapter = (JDBCAdapter)users.getConnection().getAdapter();
            JDBCClient client = adapter.getClient();

            String catalog = users.getParameter(JDBCClient.CATALOG);
            String table = catalog+"."+users.getParameter(JDBCClient.TABLE);

            String sql = "select a.cn, a.gidNumber, b.gidNumber" +
                    " from "+table+" a"+
                    " left join nis.groups b on b.domain=? and a.cn=b.cn"+
                    " where a.cn = ? and (b.gidNumber is null and a.gidNumber = ? or b.gidNumber = ?)"+
                    " order by a.cn";

            Collection<Assignment> assignments = new ArrayList<Assignment>();
            assignments.add(new Assignment(domainName));
            assignments.add(new Assignment(cn));
            assignments.add(new Assignment(gidNumber));
            assignments.add(new Assignment(gidNumber));

            QueryResponse queryResponse = new QueryResponse() {
                public void add(Object object) throws Exception {
                    ResultSet rs = (ResultSet)object;

                    String cn2 = rs.getString(1);
                    Integer gidNumber2 = (Integer)rs.getObject(3);
                    if (gidNumber2 == null) gidNumber2 = (Integer)rs.getObject(2);

                    TableItem ti = new TableItem(matchesTable, SWT.NONE);
                    ti.setText(0, domainName);
                    ti.setText(1, "" + cn2);
                    ti.setText(2, "" + gidNumber2);
                }
            };

            client.executeQuery(sql, assignments, queryResponse);
        }
    }

    public void run() throws Exception {

        groupsTable.removeAll();
        conflictsTable.removeAll();
        matchesTable.removeAll();
        conflicts.clear();

        String actionName = actionCombo.getText();
        String className = (String) actionCombo.getData(actionName);

        Class clazz = Class.forName(className);
        NISAction action = (NISAction) clazz.newInstance();

        NISActionRequest request = new NISActionRequest();
        request.setDomain(domain.getName());

        SearchRequest searchRequest = new SearchRequest();
        SearchResponse<SearchResult> searchResponse = new SearchResponse<SearchResult>();

        domains.search(searchRequest, searchResponse);

        while (searchResponse.hasNext()) {
            SearchResult result = searchResponse.next();
            Attributes attributes = result.getAttributes();
            String domain = (String) attributes.getValue("name");
            request.addDomain(domain);
        }

        NISActionResponse response = new NISActionResponse() {
            public void add(Object object) {
                Conflict conflict = (Conflict)object;

                Attributes attributes1 = conflict.getAttributes1();
                String cn = (String) attributes1.getValue("cn");

                Collection<Conflict> list = conflicts.get(cn);
                if (list == null) {
                    list = new ArrayList<Conflict>();
                    conflicts.put(cn, list);
                }

                list.add(conflict);
            }
        };

        action.execute(request, response);

        for (Collection<Conflict> list : conflicts.values()) {

            Conflict conflict = list.iterator().next();

            Attributes attributes1 = conflict.getAttributes1();
            String cn = (String) attributes1.getValue("cn");
            Integer gidNumber = (Integer)attributes1.getValue("gidNumber");
            if (gidNumber == null) gidNumber = (Integer)attributes1.getValue("origGidNumber");

            TableItem ti = new TableItem(groupsTable, SWT.NONE);
            ti.setText(0, cn);
            ti.setText(1, "" + gidNumber);

            ti.setData(attributes1);
        }

        messageLabel.setText("Found " + conflicts.size() + " groups(s).");
    }

    public void edit(
            String domain,
            String partitionName,
            String cn,
            Integer origGidNumber
    ) throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        Partitions partitions = penroseApplication.getPartitions();
        Partition partition = partitions.getPartition(partitionName);

        RDNBuilder rb = new RDNBuilder();
        rb.set("domain", domain);
        rb.set("cn", cn);
        DN dn = new DN(rb.toRdn());

        NISGroupDialog dialog = new NISGroupDialog(getSite().getShell(), SWT.NONE);
        dialog.setDomain(domain);
        dialog.setName(cn);
        dialog.setOrigGidNumber(origGidNumber);

        Source penroseGroups = partition.getSource("penrose.groups");

        SearchRequest request = new SearchRequest();
        request.setDn(dn);

        SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

        penroseGroups.search(request, response);

        if (response.hasNext()) {
            SearchResult result = response.next();
            Attributes attributes = result.getAttributes();
            dialog.setNewGidNumber((Integer)attributes.getValue("gidNumber"));
        }

        Source members = partition.getSource("cache.groups_memberUid");

        request = new SearchRequest();
        request.setFilter("(cn="+cn+")");

        response = new SearchResponse<SearchResult>();

        members.search(request, response);

        while (response.hasNext()) {
            SearchResult result = response.next();
            Attributes attributes = result.getAttributes();
            String memberUid = (String) attributes.getValue("memberUid");
            dialog.addMember(memberUid);
        }

        dialog.open();

        int action = dialog.getAction();

        if (action == NISUserDialog.CANCEL) return;

        Integer newGidNumber = dialog.getGidNumber();
        String message = dialog.getMessage();

        if (action == NISGroupDialog.SET) {

            if (!origGidNumber.equals(newGidNumber)) checkGidNumber(cn, newGidNumber);

            Attributes attrs = new Attributes();
            attrs.setValue("domain", domain);
            attrs.setValue("cn", cn);
            attrs.setValue("origGidNumber", origGidNumber);
            attrs.setValue("gidNumber", newGidNumber);
            attrs.setValue("message", message);

            penroseGroups.add(dn, attrs);

        } else if (action == NISGroupDialog.CHANGE) {

            if (!origGidNumber.equals(newGidNumber)) checkGidNumber(cn, newGidNumber);

            Collection<Modification> modifications = new ArrayList<Modification>();
            modifications.add(new Modification(Modification.REPLACE, new Attribute("gidNumber", newGidNumber)));
            modifications.add(new Modification(Modification.REPLACE, new Attribute("message", message)));

            penroseGroups.modify(dn, modifications);

        } else { // if (action == NISGroupDialog.REMOVE) {

            penroseGroups.delete(dn);
        }
    }

    public void checkGidNumber(String cn, Integer gidNumber) throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        Partitions partitions = penroseApplication.getPartitions();
        Partition partition = partitions.getPartition("DEFAULT");

        SearchRequest request = new SearchRequest();
        request.setFilter("(gidNumber=" + gidNumber + ")");

        SearchResponse<SearchResult> response = new SearchResponse<SearchResult>();

        Source groups = partition.getSource("penrose.groups");
        groups.search(request, response);

        while (response.hasNext()) {
            SearchResult result = response.next();
            Attributes attributes = result.getAttributes();

            String domainName = (String)attributes.getValue("domain");
            String cn2 = (String)attributes.getValue("cn");
            if (cn.equals(cn2)) continue;

            throw new Exception("GID number "+gidNumber+" is already allocated for user "+cn2+" in domain "+domainName);
        }

        SearchRequest searchRequest = new SearchRequest();
        SearchResponse<SearchResult> searchResponse = new SearchResponse<SearchResult>();

        domains.search(searchRequest, searchResponse);

        while (searchResponse.hasNext()) {
            SearchResult searchResults = searchResponse.next();
            Attributes attributes = searchResults.getAttributes();

            String domainName = (String) attributes.getValue("name");
            String partitionName = (String) attributes.getValue("partition");
            Partition partition2 = partitions.getPartition(partitionName);

            response = new SearchResponse<SearchResult>();

            groups = partition2.getSource("cache.groups");
            groups.search(request, response);

            while (response.hasNext()) {
                SearchResult result = response.next();
                Attributes attrs = result.getAttributes();

                String cn2 = (String)attrs.getValue("cn");
                if (cn.equals(cn2)) continue;

                throw new Exception("GID number "+gidNumber+" is used by user "+cn2+" in domain "+domainName);
            }
        }
    }
}
