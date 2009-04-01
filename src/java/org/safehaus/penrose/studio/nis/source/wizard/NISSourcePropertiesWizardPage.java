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
package org.safehaus.penrose.studio.nis.source.wizard;

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
public class NISSourcePropertiesWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    public final static String NAME = "NIS Source Settings";

    Text baseText;
    Text objectClassesText;

    private String base;
    private String objectClasses;

    public NISSourcePropertiesWizardPage() {
        super(NAME);
        setDescription("Enter the properties of the NIS source.");
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label baseLabel = new Label(composite, SWT.NONE);
        baseLabel.setText("Base:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        baseLabel.setLayoutData(gd);

        baseText = new Text(composite, SWT.BORDER);
        baseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        if (base != null) baseText.setText(base);

        baseText.addModifyListener(this);

        Label objectClassesLabel = new Label(composite, SWT.NONE);
        objectClassesLabel.setText("Object Classes:");
        gd = new GridData();
        gd.widthHint = 100;
        objectClassesLabel.setLayoutData(gd);

        objectClassesText = new Text(composite, SWT.BORDER);
        objectClassesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        if (objectClasses != null) objectClassesText.setText(objectClasses);

        setPageComplete(validatePage());
    }

    public String getBase() {
        return baseText.getText().trim();
    }

    public boolean validatePage() {
        if ("".equals(getBase())) return false;
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

    public void setBase(String base) {
        this.base = base;
    }

    public String getObjectClasses() {
        return objectClasses;
    }

    public void setObjectClasses(String objectClasses) {
        this.objectClasses = objectClasses;
    }
}