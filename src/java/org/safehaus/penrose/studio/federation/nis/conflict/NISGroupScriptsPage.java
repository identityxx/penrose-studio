package org.safehaus.penrose.studio.federation.nis.conflict;

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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.federation.nis.NISDomain;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.nis.dialog.NISGroupDialog;
import org.safehaus.penrose.studio.nis.dialog.NISUserDialog;
import org.safehaus.penrose.studio.nis.action.*;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.connection.Connection;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class NISGroupScriptsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Combo actionCombo;

    Label messageLabel;
    Table groupsTable;
    Table conflictsTable;
    Table matchesTable;

    NISGroupsEditor editor;
    NISDomain domain;
    NISFederation nisFederation;

    Map<String,Collection<Conflict>> conflicts = new TreeMap<String,Collection<Conflict>>();

    public NISGroupScriptsPage(NISGroupsEditor editor) {
        super(editor, "SCRIPTS", "  Scripts  ");

        this.editor = editor;
        domain = editor.getDomain();
        nisFederation = editor.getNisTool();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Group Scripts");

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

            actionCombo.add("Conflicting GID Finder");
            actionCombo.setData("Conflicting GID Finder", ConflictingGIDFinderAction.class.getName());

            actionCombo.add("Inconsistent GID Finder");
            actionCombo.setData("Inconsistent GID Finder", InconsistentGIDFinderAction.class.getName());
/*
            SearchRequest request = new SearchRequest();
            request.setFilter("(type=groups)");

            SearchResponse response = new SearchResponse() {
                public void add(SearchResult result) throws Exception {
                    Attributes attributes = result.getAttributes();
                    String actionName = (String) attributes.getValue("name");
                    String className = (String) attributes.getValue("className");

                    actionCombo.add(actionName);
                    actionCombo.setData(actionName, className);
                }
            };

            nisFederation.getActions().search(request, response);
*/
            actionCombo.select(0);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
                    log.error(e.getMessage(), e);
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

        groupsTable.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent focusEvent) {
                conflictsTable.deselectAll();
                matchesTable.deselectAll();
            }
        });

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
                    log.error(e.getMessage(), e);
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

        conflictsTable.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent focusEvent) {
                matchesTable.deselectAll();
            }
        });

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

        matchesTable.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent focusEvent) {
                conflictsTable.deselectAll();
            }
        });

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
                    TableItem item;

                    if (conflictsTable.getSelectionCount() > 0) {
                        item = conflictsTable.getSelection()[0];

                    } else if (matchesTable.getSelectionCount() > 0) {
                        item = matchesTable.getSelection()[0];

                    } else if (groupsTable.getSelectionCount() > 0) {
                        item = groupsTable.getSelection()[0];

                    } else {
                        return;
                    }

                    Attributes attributes = (Attributes)item.getData();
                    String domainName = (String)attributes.getValue("domain");
                    String cn = (String)attributes.getValue("cn");
                    Integer origGidNumber = (Integer)attributes.getValue("origGidNumber");

                    edit(domainName, cn, origGidNumber);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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

            ti.setData(attributes2);
        }
    }

    public void showMatches(Attributes attributes) throws Exception {

        matchesTable.removeAll();

        String cn = (String) attributes.getValue("cn");
        Integer gidNumber = (Integer) attributes.getValue("gidNumber");
        if (gidNumber == null) gidNumber = (Integer) attributes.getValue("origGidNumber");

        Project project = nisFederation.getProject();
        PartitionConfigs partitionConfigs = project.getPartitionConfigs();

        Partition partition = nisFederation.getPartition();
        Connection connection = partition.getConnection(Federation.JDBC);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        JDBCClient client = adapter.getClient();

        for (NISDomain repository : nisFederation.getRepositories()) {
            final String name = repository.getName();
            if (domain.getName().equals(name)) continue;

            PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(name);
            SourceConfig sourceConfig = partitionConfig.getSourceConfigs().getSourceConfig(NISFederation.CACHE_GROUPS);

            String table = client.getTableName(sourceConfig);

            String sql = "select a.cn, a.gidNumber, b.gidNumber" +
                    " from "+table+" a"+
                    " left join "+ NISFederation.NIS_TOOL +".groups b on b.domain=? and a.cn=b.cn"+
                    " where a.cn = ? and (b.gidNumber is null and a.gidNumber = ? or b.gidNumber = ?)"+
                    " order by a.cn";

            Collection<Assignment> assignments = new ArrayList<Assignment>();
            assignments.add(new Assignment(name));
            assignments.add(new Assignment(cn));
            assignments.add(new Assignment(gidNumber));
            assignments.add(new Assignment(gidNumber));

            QueryResponse queryResponse = new QueryResponse() {
                public void add(Object object) throws Exception {
                    ResultSet rs = (ResultSet)object;

                    String cn2 = rs.getString(1);
                    Integer origGidNumber2 = (Integer)rs.getObject(2);
                    Integer gidNumber2 = (Integer)rs.getObject(3);
                    if (gidNumber2 == null) gidNumber2 = origGidNumber2;

                    Attributes attributes = new Attributes();
                    attributes.setValue("domain", name);
                    attributes.setValue("cn", cn2);
                    attributes.setValue("origGidNumber", origGidNumber2);
                    attributes.setValue("gidNumber", gidNumber2);

                    TableItem ti = new TableItem(matchesTable, SWT.NONE);
                    ti.setText(0, name);
                    ti.setText(1, "" + cn2);
                    ti.setText(2, "" + gidNumber2);

                    ti.setData(attributes);
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
        action.setNisTool(nisFederation);

        NISActionRequest request = new NISActionRequest();
        request.setDomain(domain.getName());

        for (NISDomain repository : nisFederation.getRepositories()) {
            String name = repository.getName();
            request.addDomain(name);
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
            String domainName,
            String cn,
            Integer origGidNumber
    ) throws Exception {

        Partition partition = nisFederation.getPartitions().getPartition(domainName);

        RDNBuilder rb = new RDNBuilder();
        rb.set("domain", domainName);
        rb.set("cn", cn);
        DN dn = new DN(rb.toRdn());

        NISGroupDialog dialog = new NISGroupDialog(getSite().getShell(), SWT.NONE);
        dialog.setDomain(domainName);
        dialog.setName(cn);
        dialog.setOrigGidNumber(origGidNumber);

        Source penroseGroups = partition.getSource(NISFederation.CHANGE_GROUPS);

        SearchRequest request = new SearchRequest();
        request.setDn(dn);

        SearchResponse response = new SearchResponse();

        penroseGroups.search(request, response);

        if (response.hasNext()) {
            SearchResult result = response.next();
            Attributes attributes = result.getAttributes();
            dialog.setNewGidNumber((Integer)attributes.getValue("gidNumber"));
        }

        Source members = partition.getSource(NISFederation.CACHE_GROUPS +"_memberUid");

        request = new SearchRequest();
        request.setFilter("(cn="+cn+")");

        response = new SearchResponse();

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
            attrs.setValue("domain", domainName);
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

        Partition partition = nisFederation.getPartition();

        SearchRequest request = new SearchRequest();
        request.setFilter("(gidNumber=" + gidNumber + ")");

        SearchResponse response = new SearchResponse();

        Source groups = partition.getSource(NISFederation.CHANGE_GROUPS);
        groups.search(request, response);

        while (response.hasNext()) {
            SearchResult result = response.next();
            Attributes attributes = result.getAttributes();

            String domainName = (String)attributes.getValue("domain");
            String cn2 = (String)attributes.getValue("cn");
            if (cn.equals(cn2)) continue;

            throw new Exception("GID number "+gidNumber+" is already allocated for user "+cn2+" in domain "+domainName);
        }

        for (NISDomain repository : nisFederation.getRepositories()) {
            String name = repository.getName();

            Partition partition2 = nisFederation.getPartitions().getPartition(name);

            response = new SearchResponse();

            groups = partition2.getSource(NISFederation.CACHE_GROUPS);
            groups.search(request, response);

            while (response.hasNext()) {
                SearchResult result = response.next();
                Attributes attrs = result.getAttributes();

                String cn2 = (String)attrs.getValue("cn");
                if (cn.equals(cn2)) continue;

                throw new Exception("GID number "+gidNumber+" is used by user "+cn2+" in domain "+name);
            }
        }
    }
}
