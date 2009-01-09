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
import org.safehaus.penrose.studio.connection.wizard.ConnectionNameWizard;
import org.safehaus.penrose.studio.nis.connection.wizard.NISConnectionSettingsWizard;

import javax.naming.InitialContext;

/**
 * @author Endi S. Dewata
 */
public class NISConnectionPropertiesPage extends ConnectionEditorPage {

    Label nameText;
    Label hostnameText;
    Label domainText;

    Table parametersTable;

    String url;

    public NISConnectionPropertiesPage(NISConnectionEditor editor) {
        super(editor, "PROPERTIES", "Properties");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section propertiesSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        propertiesSection.setText("Properties");
        propertiesSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control propertiesControl = createPropertiesControl(propertiesSection);
        propertiesSection.setClient(propertiesControl);

        Section settingsSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        settingsSection.setText("Settings");
        settingsSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control settingsControl = createSettingsControl(settingsSection);
        settingsSection.setClient(settingsControl);

        refresh();
    }

    public Composite createPropertiesControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createPropertiesLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createPropertiesRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createPropertiesLeftControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Label connectionNameLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        connectionNameLabel.setLayoutData(gd);

        nameText = toolkit.createLabel(composite, "", SWT.NONE);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
/*
        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                connectionConfig.setName(nameText.getText());
                checkDirty();
            }
        });
*/
        return composite;
    }

    public Composite createPropertiesRightControl(final Composite parent) {

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
                    String name = connectionConfig.getName();

                    ConnectionNameWizard wizard = new ConnectionNameWizard();
                    wizard.setServer(server);
                    wizard.setPartitionName(partitionName);
                    wizard.setConnectionName(name);

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    String newName = wizard.getConnectionName();

                    editor.rename(name, newName);

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public Composite createSettingsControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createSettingsLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createSettingsRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createSettingsLeftControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Label hostLabel = toolkit.createLabel(composite, "Server:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        hostLabel.setLayoutData(gd);

        hostnameText = toolkit.createLabel(composite, "", SWT.NONE);
        hostnameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
/*
        hostnameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                connectionConfig.setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });
*/
        toolkit.createLabel(composite, "Domain:");

        domainText = toolkit.createLabel(composite, "", SWT.NONE);
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
/*
        domainText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String url = getURL();
                connectionConfig.setParameter(Context.PROVIDER_URL, url);
                checkDirty();
            }
        });
*/
        return composite;
    }

    public Composite createSettingsRightControl(final Composite parent) {

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
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public void refresh() {
        nameText.setText(connectionConfig.getName());

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
