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
package org.safehaus.penrose.studio.partition.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

/**
 * @author Endi S. Dewata
 */
public class PartitionProxyPage extends WizardPage implements ModifyListener {

    public final static String ACTIVE_DIRECTORY = "Active Directory";
    public final static String LDAP             = "LDAP";

    public final static String NAME = "Partition Name";

    Text nameText;

    Button mapRootDseCheckbox;
    Button mapADSchemaCheckbox;
    Button adFormatCheckbox;
    Button ldapFormatCheckbox;

    public PartitionProxyPage() {
        super(NAME);
        setDescription("Enter the name of the partition.");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        setControl(composite);

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");
        GridData gd = new GridData();
        gd.widthHint = 50;
        nameLabel.setLayoutData(gd);

        nameText = new Text(composite, SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameText.addModifyListener(this);

        Label separator = new Label(composite, SWT.NONE);
        gd = new GridData();
        gd.horizontalSpan = 2;
        separator.setLayoutData(gd);

        Composite settingsComposite = new Composite(composite, SWT.NONE);
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        settingsComposite.setLayoutData(gd);
        settingsComposite.setLayout(new GridLayout(2, false));

        Label settingsLabel = new Label(settingsComposite, SWT.NONE);
        settingsLabel.setText("Settings:");
        gd = new GridData();
        gd.horizontalSpan = 2;
        settingsLabel.setLayoutData(gd);

        mapRootDseCheckbox = new Button(settingsComposite, SWT.CHECK);
        mapRootDseCheckbox.setText("Map Root DSE");
        gd = new GridData();
        gd.horizontalSpan = 2;
        mapRootDseCheckbox.setLayoutData(gd);

        mapADSchemaCheckbox = new Button(settingsComposite, SWT.CHECK);
        mapADSchemaCheckbox.setText("Map Active Directory Schema");
        gd = new GridData();
        gd.horizontalSpan = 2;
        mapADSchemaCheckbox.setLayoutData(gd);

        mapADSchemaCheckbox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                boolean enabled = mapADSchemaCheckbox.getSelection();
                adFormatCheckbox.setEnabled(enabled);
                ldapFormatCheckbox.setEnabled(enabled);
            }
        });

        Label blank = new Label(settingsComposite, SWT.NONE);
        gd = new GridData();
        gd.widthHint = 20;
        blank.setLayoutData(gd);

        adFormatCheckbox = new Button(settingsComposite, SWT.RADIO);
        adFormatCheckbox.setText("Active Directory Format");
        adFormatCheckbox.setSelection(true);
        adFormatCheckbox.setEnabled(false);

        new Label(settingsComposite, SWT.NONE);

        ldapFormatCheckbox = new Button(settingsComposite, SWT.RADIO);
        ldapFormatCheckbox.setText("LDAP Format");
        ldapFormatCheckbox.setEnabled(false);

        setPageComplete(validatePage());
    }

    public String getPartitionName() {
        return nameText.getText().trim();
    }

    public boolean getMapRootDse() {
        return mapRootDseCheckbox.getSelection();
    }

    public boolean getMapADSchema() {
        return mapADSchemaCheckbox.getSelection();
    }

    public String getSchemaFormat() {
        return adFormatCheckbox.getSelection() ? ACTIVE_DIRECTORY : LDAP;
    }

    public boolean validatePage() {
        if ("".equals(getPartitionName())) return false;
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
