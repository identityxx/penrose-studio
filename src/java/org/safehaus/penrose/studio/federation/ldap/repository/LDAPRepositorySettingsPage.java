package org.safehaus.penrose.studio.federation.ldap.repository;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.federation.LDAPFederationClient;
import org.safehaus.penrose.federation.LDAPRepository;
import org.safehaus.penrose.studio.federation.ldap.wizard.EditLDAPRepositoryWizard;
import org.safehaus.penrose.studio.project.Project;

/**
 * @author Endi S. Dewata
 */
public class LDAPRepositorySettingsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Label urlText;
    Label suffixText;
    Label bindDnText;
    Label bindPasswordText;

    LDAPRepositoryEditor editor;
    LDAPRepository repository;
    LDAPFederationClient ldapFederation;

    Project project;

    public LDAPRepositorySettingsPage(LDAPRepositoryEditor editor) {
        super(editor, "SETTINGS", "  Settings  ");

        this.editor = editor;
        this.project = editor.project;
        this.ldapFederation = editor.ldapFederation;
        this.repository = editor.getRepository();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Settings");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section settingsSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        settingsSection.setText("LDAP Repository");
        settingsSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control settingsControl = createSettingsControl(settingsSection);
        settingsSection.setClient(settingsControl);

        refresh();
    }

    public Composite createSettingsControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite leftPanel = createSettingsLeftPanel(composite);
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightPanel = createSettingsRightPanel(composite);
        rightPanel.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        return composite;
    }

    public Composite createSettingsLeftPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label urlLabel = toolkit.createLabel(composite, "Servers:");
        urlLabel.setLayoutData(new GridData());
        GridData gd = new GridData();
        gd.widthHint = 100;
        urlLabel.setLayoutData(gd);

        urlText = toolkit.createLabel(composite, "");
        urlText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label suffixLabel = toolkit.createLabel(composite, "Suffix:");
        suffixLabel.setLayoutData(new GridData());

        suffixText = toolkit.createLabel(composite, "");
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label bindDnLabel = toolkit.createLabel(composite, "Bind DN:");
        bindDnLabel.setLayoutData(new GridData());

        bindDnText = toolkit.createLabel(composite, "");
        bindDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label bindPasswordLabel = toolkit.createLabel(composite, "Password:");
        bindPasswordLabel.setLayoutData(new GridData());

        bindPasswordText = toolkit.createLabel(composite, "");
        bindPasswordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createSettingsRightPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        Button editButton = toolkit.createButton(composite, "Edit", SWT.PUSH);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 100;
        editButton.setLayoutData(gd);

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    EditLDAPRepositoryWizard wizard = new EditLDAPRepositoryWizard(repository);

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    if (dialog.open() == Window.CANCEL) return;

                    ldapFederation.updateRepository(repository);

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
        try {
            String url = repository.getParameter(LDAPRepository.LDAP_URL);
            urlText.setText(url == null ? "" : url);

            String suffix = repository.getParameter(LDAPRepository.LDAP_SUFFIX);
            suffixText.setText(suffix == null ? "" : suffix);

            String bindDn = repository.getParameter(LDAPRepository.LDAP_USER);
            bindDnText.setText(bindDn == null ? "" : bindDn);

            String bindPassword = repository.getParameter(LDAPRepository.LDAP_PASSWORD);
            bindPasswordText.setText(bindPassword == null ? "" : "*****");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}
