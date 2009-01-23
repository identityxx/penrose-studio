package org.safehaus.penrose.studio.nis.source.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.source.editor.SourceEditorPage;
import org.safehaus.penrose.studio.nis.source.wizard.NISSourcePropertiesWizard;

public class NISSourcePropertyPage extends SourceEditorPage {

    Label connectionText;
	Label baseText;
    Label objectClassesText;

    public NISSourcePropertyPage(NISSourceEditor editor) throws Exception {
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

        refresh();
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

	public Composite createNISLeftControl(Composite parent) {

		Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Label connectionLabel = toolkit.createLabel(composite, "Connection:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        connectionLabel.setLayoutData(gd);

        connectionText = toolkit.createLabel(composite, "", SWT.READ_ONLY);
        connectionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Base:");

        baseText = toolkit.createLabel(composite, "", SWT.NONE);
        baseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Object Classes:");

        objectClassesText = toolkit.createLabel(composite, "", SWT.NONE);
        objectClassesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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
                    NISSourcePropertiesWizard wizard = new NISSourcePropertiesWizard();
                    wizard.setServer(server);
                    wizard.setPartitionName(partitionName);
                    wizard.setSourceConfig(sourceConfig);

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

        String connection = sourceConfig.getConnectionName();
        connectionText.setText(connection == null ? "" : connection);

        String base = sourceConfig.getParameter("base");
        baseText.setText(base == null ? "" : base);

        String objectClasses = sourceConfig.getParameter("objectClasses");
        objectClassesText.setText(objectClasses == null ? "" : objectClasses);
    }
}
