package org.safehaus.penrose.studio.nis.connection.editor;

import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorPage;
import org.safehaus.penrose.studio.nis.connection.wizard.NISConnectionSettingsWizard;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import javax.naming.InitialContext;

/**
 * @author Endi S. Dewata
 */
public class NISConnectionPropertiesPage extends ConnectionEditorPage {

    Label adapterText;
    Label hostnameText;
    Label domainText;

    Table parametersTable;

    String url;

    public NISConnectionPropertiesPage(NISConnectionEditor editor) {
        super(editor, "NIS", "NIS");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section nisSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        nisSection.setText("NIS");
        nisSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control nisControl = createNISControl(nisSection);
        nisSection.setClient(nisControl);
    }

    public Composite createNISControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createNISLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createNISRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createNISLeftControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        toolkit.createLabel(composite, "Adapter:");

        adapterText = toolkit.createLabel(composite, "", SWT.READ_ONLY);
        adapterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label hostLabel = toolkit.createLabel(composite, "Server:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        hostLabel.setLayoutData(gd);

        hostnameText = toolkit.createLabel(composite, "", SWT.NONE);
        hostnameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Domain:");

        domainText = toolkit.createLabel(composite, "", SWT.NONE);
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createNISRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    NISConnectionSettingsWizard wizard = new NISConnectionSettingsWizard();
                    wizard.setServer(server);
                    wizard.setPartitionName(partitionName);
                    wizard.setConnectionConfig(connectionConfig);

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    editor.store();
                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public void refresh() {

        String adapter = connectionConfig.getAdapterName();
        adapterText.setText(adapter == null ? "" : adapter);

        String url = connectionConfig.getParameter(InitialContext.PROVIDER_URL);
        String hostname = null;
        String domain = null;

        if (url != null) {
            String s[] = parseURL(url);
            hostname = s[0];
            domain = s[1];
        }

        hostnameText.setText(hostname == null ? "" : hostname);
        domainText.setText(domain == null ? "" : domain);
    }

    public String getURL() {
        String hostname = hostnameText.getText();
        String domain = domainText.getText();

        return "nis://" + hostname + "/" + domain;
    }

    public String[] parseURL(String url) {
        String s[] = new String[2];

        int i = url.indexOf("/", 6);
        s[0] = url.substring(6, i);
        s[1] = url.substring(i+1);

        return s;
    }
}
