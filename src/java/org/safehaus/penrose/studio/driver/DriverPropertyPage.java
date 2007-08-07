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
package org.safehaus.penrose.studio.driver;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.studio.driver.DriverWizard;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class DriverPropertyPage extends WizardPage implements ModifyListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Driver Property";

    Text nameText;
    Combo adapterNameCombo;

    public DriverPropertyPage() {
        super(NAME);
        setDescription("Enter the name of the driver.");
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

        Label adapterLabel = new Label(composite, SWT.NONE);
        adapterLabel.setText("Adapter:");

        adapterNameCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        adapterNameCombo.add("JDBC");
        adapterNameCombo.add("LDAP");
        adapterNameCombo.select(0);
        adapterNameCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        adapterNameCombo.addModifyListener(this);

        setPageComplete(validatePage());
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) init();
    }

    public void init() {
        try {
            DriverWizard wizard = (DriverWizard)getWizard();
            Driver driver = wizard.getDriver();
            if (driver == null) return;

            nameText.setText(driver.getName() == null ? "" : driver.getName());
            adapterNameCombo.setText(driver.getAdapterName() == null ? "" : driver.getAdapterName());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getDriverName() {
        return nameText.getText().trim();
    }

    public String getAdapterName() {
        return adapterNameCombo.getText().trim();
    }

    public boolean validatePage() {
        if ("".equals(getDriverName())) return false;
        if ("".equals(getAdapterName())) return false;
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
