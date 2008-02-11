package org.safehaus.penrose.studio.federation.editor;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.federation.*;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.studio.federation.ldap.LDAPRepository;
import org.safehaus.penrose.studio.federation.nis.NISDomain;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.project.Project;

import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class FederationDatabasePage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    FederationEditor editor;
    Federation federation;

    public FederationDatabasePage(FederationEditor editor, Federation federation) {
        super(editor, "DATABASE", "  Database  ");

        this.editor = editor;
        this.federation = federation;
    }

    public void createFormContent(IManagedForm managedForm) {
        try {
            toolkit = managedForm.getToolkit();

            ScrolledForm form = managedForm.getForm();
            form.setText("Database");

            Composite body = form.getBody();
            body.setLayout(new GridLayout());

            Section databaseSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            databaseSection.setText("Database");
            databaseSection.setLayoutData(new GridData(GridData.FILL_BOTH));

            Control databaseControl = createDatabaseControl(databaseSection);
            databaseSection.setClient(databaseControl);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public Composite createDatabaseControl(Composite parent) throws Exception {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Project project = federation.getProject();
        PenroseClient client = project.getClient();
/*
        PartitionClient partitionClient = client.getPartitionClient(Federation.PARTITION);
        ConnectionClient connectionClient = partitionClient.getConnectionClient(Federation.JDBC);
        ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();

        //Partition partition = federation.getPartition();
        //JDBCConnection connection = (JDBCConnection)partition.getConnection(Federation.JDBC);
        //ConnectionConfig connectionConfig = connection.getConnectionConfig();

        Label driverLabel = toolkit.createLabel(composite, "Driver:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        driverLabel.setLayoutData(gd);

        String driver = connectionConfig.getParameter("driver");
        Label driverText = toolkit.createLabel(composite, driver == null ? "" : driver);
        driverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label domainLabel = toolkit.createLabel(composite, "URL:");
        domainLabel.setLayoutData(new GridData());

        String url = connectionConfig.getParameter("url");
        Label domainText = toolkit.createLabel(composite, url == null ? "" : url);
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label serverLabel = toolkit.createLabel(composite, "Username:");
        serverLabel.setLayoutData(new GridData());

        String user = connectionConfig.getParameter("user");
        Label serverText = toolkit.createLabel(composite, user == null ? "" : user);
        serverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label suffixLabel = toolkit.createLabel(composite, "Password:");
        suffixLabel.setLayoutData(new GridData());

        Label suffixText = toolkit.createLabel(composite, "*****");
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
*/
/*
        Button importButton = toolkit.createButton(composite, "Import", SWT.PUSH);
        importButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    importFederationConfig();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Button exportButton = toolkit.createButton(composite, "Export", SWT.PUSH);
        exportButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    exportFederationConfig();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
*/
        return composite;
    }
/*
    public void importFederationConfig() throws Exception {

        FileDialog dialog = new FileDialog(getSite().getShell(), SWT.OPEN);
        dialog.setText("Import");
        dialog.setFilterExtensions(new String[] { "*.xml" });

        String filename = dialog.open();
        if (filename == null) return;

        File file = new File(filename);

        FederationReader reader = new FederationReader();
        FederationConfig federationConfig = reader.read(file);

        for (RepositoryConfig repository : federationConfig.getRepositoryConfigs()) {
            federation.addRepository(repository);
        }
    }

    public void exportFederationConfig() throws Exception {

        FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
        dialog.setText("Export");
        dialog.setFilterExtensions(new String[] { "*.xml" });

        String filename = dialog.open();
        if (filename == null) return;

        FederationConfig federationConfig =  new FederationConfig();

        RepositoryConfig globalRepository = federation.getGlobalRepository();
        globalRepository.setName("GLOBAL");
        globalRepository.setType("GLOBAL");
        
        federationConfig.addRepositoryConfig(globalRepository);

        LDAPFederation ldapFederation = federation.getLdapFederation();
        for (LDAPRepository repository : ldapFederation.getRepositories()) {
            federationConfig.addRepositoryConfig(repository);
        }

        NISFederation nisFederation = federation.getNisFederation();
        for (NISDomain repository : nisFederation.getRepositories()) {
            federationConfig.addRepositoryConfig(repository);
        }

        File file = new File(filename);

        if (file.exists()) {

            boolean confirm = MessageDialog.openConfirm(
                    getSite().getShell(),
                    "Confirm Export",
                    file.getName()+" already exists.\n"+
                    "Do you want to replace it?"
            );

            if (!confirm) return;
        }

        FederationWriter writer = new FederationWriter();
        writer.write(file, federationConfig);
    }
*/
}
