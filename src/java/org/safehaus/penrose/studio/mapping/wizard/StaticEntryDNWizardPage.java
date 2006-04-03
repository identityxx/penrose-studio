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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.mapping.EntryMapping;
import org.ietf.ldap.LDAPDN;

/**
 * @author Endi S. Dewata
 */
public class StaticEntryDNWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    public final static String NAME = "Entry DN";

    Text dnText;

    public StaticEntryDNWizardPage() {
        super(NAME);
        setDescription("Enter the DN of the entry.");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("DN:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        nameLabel.setLayoutData(gd);

        dnText = new Text(composite, SWT.BORDER);
        dnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dnText.addModifyListener(this);

        new Label(composite, SWT.NONE);

        Label exampleLabel = new Label(composite, SWT.NONE);
        exampleLabel.setText("Example: dc=Example,dc=com");

        setPageComplete(validatePage());
    }

    public String getDn() {
        return "".equals(dnText.getText()) ? null : dnText.getText();
    }

    public boolean validatePage() {
        String dn = getDn();
        if (dn == null || !LDAPDN.isValid(dn)) return false;
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
