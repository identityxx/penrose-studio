package org.safehaus.penrose.studio.nis;

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
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.source.editor.JDBCSearchResultDialog;
import org.safehaus.penrose.partition.PartitionManager;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.adapter.jdbc.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.util.LDAPUtil;

import java.util.Collection;
import java.util.Iterator;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class NISUsersPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Combo domain1Combo;
    Combo domain2Combo;
    Combo actionCombo;
    Table table;

    NISEditor editor;

    public NISUsersPage(NISEditor editor) {
        super(editor, "USERS", "  Users  ");

        this.editor = editor;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Users");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Domains");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control sourcesSection = createDomainsSection(section);
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
           domain1Combo.removeAll();
           domain2Combo.removeAll();

           PenroseApplication penroseApplication = PenroseApplication.getInstance();
           PenroseContext penroseContext = penroseApplication.getPenroseContext();
           PartitionManager partitionManager = penroseContext.getPartitionManager();

           Collection partitions = partitionManager.getPartitions();
           for (Iterator i=partitions.iterator(); i.hasNext(); ) {
               Partition partition = (Partition)i.next();

               domain1Combo.add(partition.getName());
               domain2Combo.add(partition.getName());
           }

       } catch (Exception e) {
           log.debug(e.getMessage(), e);
           String message = e.toString();
           if (message.length() > 500) {
               message = message.substring(0, 500) + "...";
           }
           MessageDialog.openError(editor.getSite().getShell(), "Browse Failed", message);
       }
   }

    public Composite createDomainsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label actionLabel = toolkit.createLabel(composite, "Action:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        actionLabel.setLayoutData(gd);

        actionCombo = new Combo(composite, SWT.READ_ONLY);
        actionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        actionCombo.add("Find users from different domains with conflicting UIDs");
        actionCombo.add("Find users with inconsistent UID numbers across domains");

        Label source1Label = toolkit.createLabel(composite, "Domain 1:");
        gd = new GridData();
        gd.widthHint = 100;
        source1Label.setLayoutData(gd);

        domain1Combo = new Combo(composite, SWT.READ_ONLY);
        domain1Combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label source2Label = toolkit.createLabel(composite, "Domain 2:");
        gd = new GridData();
        gd.widthHint = 100;
        source2Label.setLayoutData(gd);

        domain2Combo = new Combo(composite, SWT.READ_ONLY);
        domain2Combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(composite, SWT.NONE);

        Button button = new Button(composite, SWT.PUSH);
        button.setText("Find");
        gd = new GridData();
        gd.widthHint = 80;
        button.setLayoutData(gd);

        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    run();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Browse Failed", message);
                }
            }
        });

        return composite;
    }

    public Composite createResultsSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout());

        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(gd);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tc = new TableColumn(table, SWT.NONE);
        tc.setText("Domain 1 uid");
        tc.setWidth(150);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("Domain 1 uidNumber");
        tc.setWidth(150);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("Domain 2 uid");
        tc.setWidth(150);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("Domain 2 uidNumber");
        tc.setWidth(150);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayout(new RowLayout());
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button edit1Button = new Button(buttons, SWT.PUSH);
        edit1Button.setText("Edit user from domain 1");

        edit1Button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    TableItem item = table.getSelection()[0];
                    Partition partition = (Partition)item.getData("partition1");
                    Source source = (Source)item.getData("source1");
                    Object uid = item.getData("uid1");
                    edit(partition, source, uid);

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Browse Failed", message);
                }
            }
        });

        Button edit2Button = new Button(buttons, SWT.PUSH);
        edit2Button.setText("Edit user from domain 2");

        edit2Button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    TableItem item = table.getSelection()[0];
                    Partition partition = (Partition)item.getData("partition2");
                    Source source = (Source)item.getData("source2");
                    Object uid = item.getData("uid2");
                    edit(partition, source, uid);

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    String message = e.toString();
                    if (message.length() > 500) {
                        message = message.substring(0, 500) + "...";
                    }
                    MessageDialog.openError(editor.getSite().getShell(), "Browse Failed", message);
                }
            }
        });

        return composite;
    }

    public void run() throws Exception {

        table.removeAll();

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        PartitionManager partitionManager = penroseContext.getPartitionManager();
        SourceManager sourceManager = penroseContext.getSourceManager();

        String partition1Name = domain1Combo.getText();
        final Partition partition1 = partitionManager.getPartition(partition1Name);
        final Source source1 = sourceManager.getSource(partition1, "users_cache");

        String partition2Name = domain2Combo.getText();
        final Partition partition2 = partitionManager.getPartition(partition2Name);
        final Source source2 = sourceManager.getSource(partition2, "users_cache");

        JDBCAdapter adapter1 = (JDBCAdapter)source1.getConnection().getAdapter();
        JDBCClient client1 = adapter1.getClient();
        String catalog1 = client1.getConnection().getCatalog();
        String table1Name = catalog1+"."+source1.getParameter(JDBCClient.TABLE);

        JDBCAdapter adapter2 = (JDBCAdapter)source2.getConnection().getAdapter();
        JDBCClient client2 = adapter2.getClient();
        String catalog2 = client2.getConnection().getCatalog();
        String table2Name = catalog2+"."+source2.getParameter(JDBCClient.TABLE);

        String sql = "select a.uid, a.uidNumber, b.uid, b.uidNumber" +
            " from "+table1Name+" a, "+table2Name+" b where ";

        if (actionCombo.getSelectionIndex() == 0) {
            sql += "a.uid <> b.uid and a.uidNumber = b.uidNumber";
        } else {
            sql += "a.uid = b.uid and a.uidNumber <> b.uidNumber";
        }

        QueryResponse response = new QueryResponse() {
            public void add(Object object) throws Exception {
                ResultSet rs = (ResultSet)object;

                Object uid1 = rs.getObject(1);
                Object uidNumber1 = rs.getObject(2);

                Object uid2 = rs.getObject(3);
                Object uidNumber2 = rs.getObject(4);

                TableItem ti = new TableItem(table, SWT.NONE);
                ti.setText(0, uid1.toString());
                ti.setText(1, uidNumber1.toString());
                ti.setText(2, uid2.toString());
                ti.setText(3, uidNumber2.toString());

                ti.setData("partition1", partition1);
                ti.setData("source1", source1);
                ti.setData("uid1", uid1);

                ti.setData("partition2", partition2);
                ti.setData("source2", source2);
                ti.setData("uid2", uid2);
            }
        };

        client1.executeQuery(sql, response);
    }

    public void edit(Partition partition, Source source, Object uid) throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        SourceManager sourceManager = penroseContext.getSourceManager();
        Source sourceUidNumber = sourceManager.getSource(partition, "users_uidNumber");

        RDNBuilder rb = new RDNBuilder();
        rb.set("uid", uid);
        RDN rdn = rb.toRdn();
        DN dn = new DN(rdn);

        JDBCSearchResultDialog dialog = new JDBCSearchResultDialog(getSite().getShell(), SWT.NONE);
        dialog.setSourceConfig(sourceUidNumber.getSourceConfig());
        dialog.setRdn(rdn);
        dialog.open();

        if (dialog.getAction() == JDBCSearchResultDialog.CANCEL) return;

        Attributes attributes = dialog.getAttributes();
        sourceUidNumber.add(dn, attributes);
    }

    public String getCatalog() {
        return "".equals(domain1Combo.getText()) ? null : domain1Combo.getText();
    }

    public String getSchema() {
        return "".equals(domain2Combo.getText()) ? null : domain2Combo.getText();
    }
}
