package org.safehaus.penrose.studio.federation.ldap.repository;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.studio.federation.ldap.LDAPRepository;
import org.safehaus.penrose.studio.federation.ldap.editor.LDAPRepositoryDialog;
import org.safehaus.penrose.studio.federation.ldap.wizard.LDAPRepositoryEditorWizard;
import org.safehaus.penrose.studio.project.Project;

import javax.naming.Context;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Endi S. Dewata
 */
public class LDAPRepositorySettingsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Label urlText;
    Label bindDnText;
    Label bindPasswordText;
    Label suffixText;

    LDAPRepositoryEditor editor;
    LDAPRepository repository;
    LDAPFederation ldapFederation;
    Project project;

    public LDAPRepositorySettingsPage(LDAPRepositoryEditor editor) {
        super(editor, "SETTINGS", "  Settings  ");

        this.editor = editor;
        this.ldapFederation = editor.ldapFederation;
        this.project = ldapFederation.getProject();
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

        new Label(body, SWT.NONE);

        Section partitionSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        partitionSection.setText("Partition");
        partitionSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control partitionControl = createPartitionControl(partitionSection);
        partitionSection.setClient(partitionControl);

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
                    LDAPRepositoryEditorWizard wizard = new LDAPRepositoryEditorWizard();
                    wizard.setWindowTitle("LDAP Repository");

                    wizard.setSuffix(repository.getSuffix());

                    Map<String,String> parameters = new LinkedHashMap<String,String>();
                    parameters.put(Context.PROVIDER_URL, repository.getUrl());
                    parameters.put(Context.SECURITY_PRINCIPAL, repository.getUser());
                    parameters.put(Context.SECURITY_CREDENTIALS, repository.getPassword());
                    wizard.setParameters(parameters);

                    wizard.setProject(project);
                    
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

                    WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    if (dialog.open() != Window.OK) return;
/*
                    LDAPRepositoryDialog dialog = new LDAPRepositoryDialog(editor.getSite().getShell(), SWT.NONE);
                    dialog.setRepository(repository);
                    dialog.open();

                    int action = dialog.getAction();
                    if (action == LDAPRepositoryDialog.CANCEL) return;
*/
                    repository.setUrl(parameters.get(Context.PROVIDER_URL));
                    repository.setUser(parameters.get(Context.SECURITY_PRINCIPAL));
                    repository.setPassword(parameters.get(Context.SECURITY_CREDENTIALS));
                    repository.setSuffix(wizard.getSuffix());

                    PenroseClient penroseClient = project.getClient();

                    penroseClient.stopPartition(repository.getName());
                    ldapFederation.removePartition(repository);

                    ldapFederation.removePartitionConfig(repository.getName());
                    project.removeDirectory("partitions/"+repository.getName());

                    ldapFederation.updateRepository(repository);

                    ldapFederation.createPartitionConfig(repository);
                    project.upload("partitions/"+repository.getName());

                    penroseClient.startPartition(repository.getName());

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public Composite createPartitionControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite left = createPartitionLeftPanel(composite);
        left.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite right = createPartitionRightPanel(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        right.setLayoutData(gd);

        return composite;
    }

    public Composite createPartitionLeftPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label suffixLabel = toolkit.createLabel(composite, "Suffix:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        suffixLabel.setLayoutData(gd);

        suffixText = toolkit.createLabel(composite, "");
        suffixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createPartitionRightPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Button createButton = toolkit.createButton(composite, "Create", SWT.PUSH);
        createButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Creating Partition",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    PenroseClient penroseClient = project.getClient();
                    ldapFederation.createPartitionConfig(repository);
                    project.upload("partitions/"+repository.getName());

                    penroseClient.startPartition(repository.getName());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button removeButton = toolkit.createButton(composite, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent selectionEvent) {
                try {
                    boolean confirm = MessageDialog.openQuestion(
                            editor.getSite().getShell(),
                            "Removing Partition",
                            "Are you sure?"
                    );

                    if (!confirm) return;

                    PenroseClient penroseClient = project.getClient();
                    penroseClient.stopPartition(repository.getName());
                    ldapFederation.removePartition(repository);

                    ldapFederation.removePartitionConfig(repository.getName());
                    project.removeDirectory("partitions/"+repository.getName());

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
            String url = repository.getUrl();
            urlText.setText(url == null ? "" : url);

            String bindDn = repository.getUser();
            bindDnText.setText(bindDn == null ? "" : bindDn);

            String bindPassword = repository.getPassword();
            bindPasswordText.setText(bindPassword == null ? "" : "*****");

            String suffix = repository.getSuffix();
            suffixText.setText(suffix == null ? "" : suffix);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}
