/**
 * Copyright (c) 2000-2006, Identyx Corporation.
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

/**
 * @author Endi S. Dewata
 */
public class FieldConditionWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    public final static String NAME = "Field Condition";

    Button requiredButton;
    Text conditionText;

    boolean required = true;
    String condition;

    public FieldConditionWizardPage() {
        super(NAME);
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label requiredLabel = new Label(composite, SWT.NONE);
        requiredLabel.setText("Required:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        requiredLabel.setLayoutData(gd);

        requiredButton = new Button(composite, SWT.CHECK);
        requiredButton.setSelection(required);
        requiredButton.addSelectionListener(this);

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Condition:");
        gd = new GridData();
        gd.verticalAlignment = SWT.BEGINNING;
        gd.widthHint = 100;
        nameLabel.setLayoutData(gd);

        conditionText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        conditionText.setText(condition == null ? "" : condition);
        conditionText.setLayoutData(new GridData(GridData.FILL_BOTH));
        conditionText.addModifyListener(this);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
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

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getCondition() {
        String s = conditionText.getText().trim();
        return s.equals("") ? null : s;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return requiredButton.getSelection();
    }
}