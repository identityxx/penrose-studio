package org.safehaus.penrose.studio.partition.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Endi S. Dewata
 */
public class PartitionPropertiesPage extends PartitionEditorPage {

    Text nameText;
    Text descriptionText;
    Button enabledCheckbox;

    public PartitionPropertiesPage(PartitionEditor editor) {
        super(editor, "PROPERTIES", "  Properties  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Properties");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control propertiesControl = createPropertiesControl(section);
        section.setClient(propertiesControl);
    }

    public void refresh() {
        nameText.setText(partitionConfig.getName() == null ? "" : partitionConfig.getName());
        descriptionText.setText(partitionConfig.getDescription() == null ? "" : partitionConfig.getDescription());
        enabledCheckbox.setSelection(partitionConfig.isEnabled());
    }

    public Composite createPropertiesControl(final Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label connectionNameLabel = getToolkit().createLabel(composite, "Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        connectionNameLabel.setLayoutData(gd);

        nameText = getToolkit().createText(composite, "", SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                partitionConfig.setName("".equals(nameText.getText()) ? null : nameText.getText());
                checkDirty();
            }
        });

        Label descriptionLabel = getToolkit().createLabel(composite, "Description:");
        gd = new GridData();
        gd.widthHint = 100;
        descriptionLabel.setLayoutData(gd);

        descriptionText = getToolkit().createText(composite, "", SWT.BORDER);
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                partitionConfig.setDescription("".equals(descriptionText.getText()) ? null : descriptionText.getText());
                checkDirty();
            }
        });

        Label enabledLabel = getToolkit().createLabel(composite, "Enabled:");
        gd = new GridData();
        gd.widthHint = 100;
        enabledLabel.setLayoutData(gd);

        enabledCheckbox = getToolkit().createButton(composite, "", SWT.CHECK);
        enabledCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        enabledCheckbox.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                partitionConfig.setEnabled(enabledCheckbox.getSelection());
                checkDirty();
            }
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        return composite;
    }
}
