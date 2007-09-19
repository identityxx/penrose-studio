package org.safehaus.penrose.studio.nis.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.connection.ConnectionConfig;

import javax.naming.Context;

/**
 * @author Endi S. Dewata
 */
public class NISToolPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    NISEditor editor;
    NISTool nisTool;

    public NISToolPage(NISEditor editor, NISTool nisTool) {
        super(editor, "SETTINGS", "  Settings  ");

        this.editor = editor;
        this.nisTool = nisTool;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Settings");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section databaseSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        databaseSection.setText("Database");
        databaseSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control databaseControl = createDatabaseControl(databaseSection);
        databaseSection.setClient(databaseControl);

        Section ldapSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        ldapSection.setText("LDAP");
        ldapSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control ldapControl = createLDAPControl(ldapSection);
        ldapSection.setClient(ldapControl);
    }

    public Composite createDatabaseControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Partition partition = nisTool.getNisPartition();
        Connection connection = partition.getConnection(NISTool.NIS_CONNECTION_NAME);
        ConnectionConfig connectionConfig = connection.getConnectionConfig();

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

        return composite;
    }

    public Composite createLDAPControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Partition partition = nisTool.getNisPartition();
        Connection connection = partition.getConnection(NISTool.LDAP_CONNECTION_NAME);
        ConnectionConfig connectionConfig = connection.getConnectionConfig();

        Label urlLabel = toolkit.createLabel(composite, "URL:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        urlLabel.setLayoutData(gd);

        String url = connectionConfig.getParameter(Context.PROVIDER_URL);
        Label urlText = toolkit.createLabel(composite, url == null ? "" : url);
        urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label bindDnLabel = toolkit.createLabel(composite, "Bind DN:");
        bindDnLabel.setLayoutData(new GridData());

        String bindDn = connectionConfig.getParameter(Context.SECURITY_PRINCIPAL);
        Label serverText = toolkit.createLabel(composite, bindDn == null ? "" : bindDn);
        serverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label bindPasswordLabel = toolkit.createLabel(composite, "Password:");
        bindPasswordLabel.setLayoutData(new GridData());

        Label bindPassword = toolkit.createLabel(composite, "*****");
        bindPassword.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

}
