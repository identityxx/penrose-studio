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
import org.safehaus.penrose.studio.nis.action.*;
import org.safehaus.penrose.studio.source.editor.JDBCSearchResultDialog;
import org.safehaus.penrose.partition.PartitionManager;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.ldap.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISUsersPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Combo actionCombo;
    List domainsList;
    Table table;

    NISEditor editor;

    Collection actions = new ArrayList();

    public NISUsersPage(NISEditor editor) {
        super(editor, "USERS", "  Users  ");

        this.editor = editor;

        actions.add(new ConflictingUIDFinderAction());
        actions.add(new InconsistentUIDFinderAction());
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("NIS Users");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Action");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

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
           domainsList.removeAll();

           PenroseApplication penroseApplication = PenroseApplication.getInstance();
           PenroseContext penroseContext = penroseApplication.getPenroseContext();
           PartitionManager partitionManager = penroseContext.getPartitionManager();

           Collection partitions = partitionManager.getPartitions();
           for (Iterator i=partitions.iterator(); i.hasNext(); ) {
               Partition partition = (Partition)i.next();
               String partitionName = partition.getName();
               if ("DEFAULT".equals(partitionName)) continue;
               domainsList.add(partitionName);
           }

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
        composite.setLayout(new GridLayout(2, false));

        Label actionLabel = toolkit.createLabel(composite, "Action:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        actionLabel.setLayoutData(gd);

        actionCombo = new Combo(composite, SWT.READ_ONLY);
        actionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        for (Iterator i=actions.iterator(); i.hasNext(); ) {
            NISAction action = (NISAction)i.next();
            actionCombo.add(action.getName());
            actionCombo.setData(action.getName(), action);
        }

        actionCombo.select(0);

        Label domainLabel = toolkit.createLabel(composite, "Domain:");
        gd = new GridData();
        gd.verticalAlignment = GridData.BEGINNING;
        gd.widthHint = 100;
        domainLabel.setLayoutData(gd);

        domainsList = new List(composite, SWT.BORDER | SWT.MULTI);
        domainsList.setLayoutData(new GridData(GridData.FILL_BOTH));

        new Label(composite, SWT.NONE);
        
        Button button = new Button(composite, SWT.PUSH);
        button.setText("Run");
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
                    MessageDialog.openError(editor.getSite().getShell(), "Action Failed", message);
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
        tc.setText("Domain 1");
        tc.setWidth(150);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("User");
        tc.setWidth(150);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("Domain 2");
        tc.setWidth(150);

        tc = new TableColumn(table, SWT.NONE);
        tc.setText("User");
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
                    MessageDialog.openError(editor.getSite().getShell(), "Edit Failed", message);
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
                    MessageDialog.openError(editor.getSite().getShell(), "Edit Failed", message);
                }
            }
        });

        return composite;
    }

    public void run() throws Exception {

        table.removeAll();

        String actionName = actionCombo.getText();
        NISAction action = (NISAction)actionCombo.getData(actionName);
        
        NISActionRequest request = new NISActionRequest();

        String[] domains = domainsList.getSelection();
        for (int i=0; i<domains.length; i++) {
            request.addDomain(domains[i]);
        }

        NISActionResponse response = new NISActionResponse() {

            int counter = 1;

            public void add(Object object) {
                Attributes attributes = (Attributes)object;

                log.debug("Displaying result #"+counter);

                Partition partition1 = (Partition)attributes.getValue("partition1");
                Source source1 = (Source)attributes.getValue("source1");
                Object uid1 = attributes.getValue("uid1");
                Object uidNumber1 = attributes.getValue("uidNumber1");

                Partition partition2 = (Partition)attributes.getValue("partition2");
                Source source2 = (Source)attributes.getValue("source2");
                Object uid2 = attributes.getValue("uid2");
                Object uidNumber2 = attributes.getValue("uidNumber2");

                TableItem ti = new TableItem(table, SWT.NONE);
                ti.setText(0, partition1.getName());
                ti.setText(1, uid1+" ("+uidNumber1+")");
                ti.setText(2, partition2.getName());
                ti.setText(3, uid2+" ("+uidNumber2+")");

                ti.setData("partition1", partition1);
                ti.setData("source1", source1);
                ti.setData("uid1", uid1);

                ti.setData("partition2", partition2);
                ti.setData("source2", source2);
                ti.setData("uid2", uid2);

                counter++;
            }
        };

        action.execute(request, response);
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
        dialog.setPartition(partition);
        dialog.setSourceConfig(sourceUidNumber.getSourceConfig());
        dialog.setRdn(rdn);
        dialog.open();

        if (dialog.getAction() == JDBCSearchResultDialog.CANCEL) return;

        Attributes attributes = dialog.getAttributes();
        sourceUidNumber.add(dn, attributes);
    }
}
