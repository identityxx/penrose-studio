package org.safehaus.penrose.studio.federation.editor;

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
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.connection.ConnectionConfig;

import javax.naming.Context;

/**
 * @author Endi S. Dewata
 */
public class FederationLDAPPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    FederationEditor editor;
    Federation federation;

    public FederationLDAPPage(FederationEditor editor, Federation federation) {
        super(editor, "LDAP", "  LDAP  ");

        this.editor = editor;
        this.federation = federation;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("LDAP");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section ldapSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        ldapSection.setText("LDAP");
        ldapSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control ldapControl = createLDAPControl(ldapSection);
        ldapSection.setClient(ldapControl);
    }

    public Composite createLDAPControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Partition partition = federation.getPartition();
        Connection connection = partition.getConnection(Federation.LDAP);
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
