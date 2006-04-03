/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.studio.service;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

/**
 * @author Endi S. Dewata
 */
public class ServiceWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    public final static String NAME = "Service Property";

    Text nameText;
    Text classText;
    Text descriptionText;
    Button enabledCheckbox;

    public ServiceWizardPage() {
        super(NAME);
        setDescription("Enter the name of the service.");
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        nameLabel.setLayoutData(gd);

        nameText = new Text(composite, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameText.addModifyListener(this);

        Label classLabel = new Label(composite, SWT.NONE);
        classLabel.setText("Class:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        classLabel.setLayoutData(gd);

        classText = new Text(composite, SWT.BORDER);
        classText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        classText.addModifyListener(this);

        Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setText("Description:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        descriptionLabel.setLayoutData(gd);

        descriptionText = new Text(composite, SWT.BORDER);
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        descriptionText.addModifyListener(this);

        Label enabledLabel = new Label(composite, SWT.NONE);
        enabledLabel.setText("Enabled:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        enabledLabel.setLayoutData(gd);

        enabledCheckbox = new Button(composite, SWT.CHECK);
        enabledCheckbox.setSelection(true);
        enabledCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        enabledCheckbox.addSelectionListener(this);

        setPageComplete(validatePage());
    }

    public String getServiceName() {
        return nameText.getText().trim();
    }

    public String getServiceClass() {
        return classText.getText().trim();
    }

    public String getDescription() {
        return descriptionText.getText().trim();
    }

    public boolean isEnabled() {
        return enabledCheckbox.getSelection();
    }

    public boolean validatePage() {
        if ("".equals(getServiceName())) return false;
        if ("".equals(getServiceClass())) return false;
        return true;
    }

    public void widgetSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
