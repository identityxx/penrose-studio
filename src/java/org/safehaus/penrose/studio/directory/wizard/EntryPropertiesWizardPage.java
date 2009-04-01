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
package org.safehaus.penrose.studio.directory.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi S. Dewata
 */
public class EntryPropertiesWizardPage extends WizardPage implements ModifyListener {

    Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "Entry Properties";

    Text nameText;
    Combo classCombo;
    Button enabledButton;
    Text descriptionText;

    private String name;
    private String className;
    private boolean enabled;
    private String description;

    public EntryPropertiesWizardPage() {
        super(NAME);
        setDescription("Enter the properties of the entry.");
    }

    public void createControl(final Composite parent) {
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
        classCombo.add("");
        classCombo.add("org.safehaus.penrose.directory.DynamicEntry");
        classCombo.add("org.safehaus.penrose.directory.ProxyEntry");

        classCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        classCombo.addModifyListener(this);

        Label enabledLabel = new Label(composite, SWT.NONE);
        enabledLabel.setText("Enabled:");
        enabledLabel.setLayoutData(new GridData());

        enabledButton = new Button(composite, SWT.CHECK);
        enabledButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                validatePage();
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
        enabledButton.setSelection(enabled);
        descriptionText.setText(description == null ? "" : description);
    }

    public String getEntryName() {
        String name = nameText.getText();
        return "".equals(name) ? null : name;
    }

    public void setEntryName(String name) {
        this.name = name;
    }

    public String getClassName() {
        String className = classCombo.getText();
        return "".equals(className) ? null : className;
    }

    public void setClassName(String entryClass) {
        this.className = entryClass;
    }

    public boolean isEnabled() {
        return enabledButton.getSelection();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEntryDescription() {
        String description = descriptionText.getText();
        return "".equals(description) ? null : description;
    }

    public void setEntryDescription(String description) {
        this.description = description;
    }
}