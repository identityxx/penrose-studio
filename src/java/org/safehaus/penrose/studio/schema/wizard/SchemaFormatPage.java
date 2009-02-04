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
package org.safehaus.penrose.studio.schema.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

/**
 * @author Endi S. Dewata
 */
public class SchemaFormatPage extends WizardPage implements ModifyListener {

    public final static String NAME = "Schema Format";

    public final static String ACTIVE_DIRECTORY = "Active Directory";
    public final static String LDAP             = "LDAP";

    Button adFormatCheckbox;
    Button ldapFormatCheckbox;

    public SchemaFormatPage() {
        super(NAME);
        setDescription("Select the type of the schema.");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        setControl(composite);

        Label settingsLabel = new Label(composite, SWT.NONE);
        settingsLabel.setText("Schema Format:");
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        settingsLabel.setLayoutData(gd);

        Label blank = new Label(composite, SWT.NONE);
        gd = new GridData();
        gd.widthHint = 20;
        blank.setLayoutData(gd);

        adFormatCheckbox = new Button(composite, SWT.RADIO);
        adFormatCheckbox.setText("Active Directory");
        adFormatCheckbox.setSelection(true);

        new Label(composite, SWT.NONE);

        ldapFormatCheckbox = new Button(composite, SWT.RADIO);
        ldapFormatCheckbox.setText("LDAP");

        setPageComplete(validatePage());
    }

    public String getFormat() {
        return adFormatCheckbox.getSelection() ? ACTIVE_DIRECTORY : LDAP;
    }

    public boolean validatePage() {
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}