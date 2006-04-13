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
package org.safehaus.penrose.studio.mapping.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.ietf.ldap.LDAPDN;
import org.safehaus.penrose.partition.Partition;

/**
 * @author Endi S. Dewata
 */
public class StaticEntryDNWizardPage extends WizardPage implements ModifyListener {

    public final static String NAME = "Entry DN";

    Text dnText;

    Partition partition;

    public StaticEntryDNWizardPage(Partition partition) {
        super(NAME);

        this.partition = partition;

        setDescription("Enter the DN of the entry.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 3;
        composite.setLayout(sectionLayout);

        Label dnLabel = new Label(composite, SWT.NONE);
        dnLabel.setText("DN:");
        GridData gd = new GridData();
        gd.widthHint = 50;
        dnLabel.setLayoutData(gd);

        dnText = new Text(composite, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        dnText.setLayoutData(gd);
        dnText.addModifyListener(this);

        new Label(composite, SWT.NONE);

        Label exampleLabel = new Label(composite, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        exampleLabel.setLayoutData(gd);
        exampleLabel.setText("Example: dc=Example,dc=com");

        setPageComplete(validatePage());
    }

    public String getDn() {
        return dnText.getText();
    }

    public boolean validatePage() {
        return LDAPDN.isValid(getDn());
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
