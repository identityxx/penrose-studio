package org.safehaus.penrose.studio.directory.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.studio.directory.wizard.EntryPropertiesWizard;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class EntryPropertiesPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Label nameText;
    Label classText;
    Button enabledCheckbox;
    Text descriptionText;

    EntryEditor editor;
	EntryConfig entryConfig;

    public EntryPropertiesPage(EntryEditor editor) {
        super(editor, "PROPERTIES", "  Properties  ");

        this.editor = editor;
        this.entryConfig = editor.entryConfig;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Entry Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section propertiesSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        propertiesSection.setText("Properties");
        propertiesSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control propertiesControl = createPropertiesControl(propertiesSection);
        propertiesSection.setClient(propertiesControl);
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

        Label nameLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        nameLabel.setLayoutData(gd);

        nameText = toolkit.createLabel(composite, "", SWT.NONE);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Class:");

        classText = toolkit.createLabel(composite, "", SWT.NONE);
        classText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.createLabel(composite, "Enabled:");

        enabledCheckbox = toolkit.createButton(composite, "", SWT.CHECK);
        enabledCheckbox.setEnabled(false);

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        Label descriptionLabel = toolkit.createLabel(composite, "Description:");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        descriptionLabel.setLayoutData(gd);

        descriptionText = toolkit.createText(composite, "", SWT.READ_ONLY | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 100;
        gd.horizontalSpan = 2;
        descriptionText.setLayoutData(gd);

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

        GridData gd = new GridData();
        gd.widthHint = 100;
        editButton.setLayoutData(gd);

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    EntryPropertiesWizard wizard = new EntryPropertiesWizard();
                    wizard.setEntryConfig(entryConfig);

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

    public void setActive(boolean b) {
        super.setActive(b);
        if (b) refresh();
    }

    public void refresh() {

        String name = entryConfig.getName();
        nameText.setText(name == null ? "" : name);

        String className = entryConfig.getEntryClass();
        classText.setText(className == null ? "" : className);

        enabledCheckbox.setSelection(entryConfig.isEnabled());

        String description = entryConfig.getDescription();
        descriptionText.setText(description == null ? "" : description);
    }
}
