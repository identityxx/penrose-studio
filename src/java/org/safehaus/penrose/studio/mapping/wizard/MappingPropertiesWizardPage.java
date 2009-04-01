/**
 * Copyright 2009 Red Hat, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.safehaus.penrose.studio.mapping.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

/**
 * @author Endi S. Dewata
 */
public class MappingPropertiesWizardPage extends WizardPage implements ModifyListener {

    public final static String NAME = "Mapping Properties";

    Text nameText;
    Combo classCombo;
    Button enabledCheckbox;
    Text descriptionText;

    private String name;
    private String className;
    private boolean enabled;
    private String description;

    public MappingPropertiesWizardPage() {
        super(NAME);
        setDescription("Enter the properties of the mapping.");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");
        nameLabel.setLayoutData(new GridData());

        nameText = new Text(composite, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameText.addModifyListener(this);

        Label classLabel = new Label(composite, SWT.NONE);
        classLabel.setText("Class:");
        classLabel.setLayoutData(new GridData());

        classCombo = new Combo(composite, SWT.BORDER);
        classCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        classCombo.addModifyListener(this);

        Label enabledLabel = new Label(composite, SWT.NONE);
        enabledLabel.setText("Enabled:");
        enabledLabel.setLayoutData(new GridData());

        enabledCheckbox = new Button(composite, SWT.CHECK);
        enabledCheckbox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setPageComplete(validatePage());
            }
        });

        Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setText("Description:");

        GridData gd = new GridData();
        gd.verticalAlignment = GridData.BEGINNING;
        descriptionLabel.setLayoutData(gd);

        descriptionText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        descriptionText.setLayoutData(new GridData(GridData.FILL_BOTH));
        descriptionText.addModifyListener(this);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if (nameText.getText().equals("")) return false;
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
        nameText.setText(name == null ? "" : name);
        classCombo.setText(className == null ? "" : className);
        enabledCheckbox.setSelection(enabled);
        descriptionText.setText(description == null ? "" : description);
    }

    public String getMappingName() {
        String name = nameText.getText();
        return "".equals(name) ? null : name;
    }

    public void setMappingName(String name) {
        this.name = name;
    }

    public String getClassName() {
        String className = classCombo.getText();
        return "".equals(className) ? null : className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isEnabled() {
        return enabledCheckbox.getSelection();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMappingDescription() {
        String description = descriptionText.getText();
        return "".equals(description) ? null : description;
    }

    public void setMappingDescription(String description) {
        this.description = description;
    }
}