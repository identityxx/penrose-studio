package org.safehaus.penrose.studio.federation.nis.domain;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
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
import org.safehaus.penrose.federation.NISDomain;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.federation.nis.wizard.EditNISDomainWizard;
import org.safehaus.penrose.studio.project.Project;

/**
 * @author Endi S. Dewata
 */
public class NISDomainSettingsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Label serverText;
    Label domainText;

    NISDomainEditor editor;
    NISFederationClient nisFederation;
    FederationRepositoryConfig domain;

    Project project;

    public NISDomainSettingsPage(NISDomainEditor editor) {
        super(editor, "SETTINGS", "  Settings  ");

        this.editor = editor;
        this.project = editor.project;
        this.nisFederation = editor.getNisFederation();
        this.domain = editor.getDomain();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Settings");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section domainSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        domainSection.setText("NIS Domain");
        domainSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control domainControl = createDomainsSection(domainSection);
        domainSection.setClient(domainControl);

        refresh();
    }

    public Composite createDomainsSection(Composite parent) {

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

        Label serverLabel = toolkit.createLabel(composite, "Server:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        serverLabel.setLayoutData(gd);

        serverText = toolkit.createLabel(composite, "");
        serverText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label domainLabel = toolkit.createLabel(composite, "Domain:");
        domainLabel.setLayoutData(new GridData());
        domainLabel.setLayoutData(new GridData());

        domainText = toolkit.createLabel(composite, "");
        domainText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createSettingsRightPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 100;
        editButton.setLayoutData(gd);

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    EditNISDomainWizard wizard = new EditNISDomainWizard(domain);
                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    if (dialog.open() == Window.CANCEL) return;

                    nisFederation.updateRepository(domain);

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
            String server = domain.getParameter(NISDomain.SERVER);
            serverText.setText(server == null ? "" : server);

            String fullName = domain.getParameter(NISDomain.DOMAIN);
            domainText.setText(fullName == null ? "" : fullName);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}
