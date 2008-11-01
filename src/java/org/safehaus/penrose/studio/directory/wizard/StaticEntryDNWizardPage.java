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
package org.safehaus.penrose.studio.directory.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;
import org.ietf.ldap.LDAPDN;

/**
 * @author Endi S. Dewata
 */
public class StaticEntryDNWizardPage extends WizardPage implements ModifyListener {

    public final static String NAME = "Entry DN";

    Text dnText;
    Combo classNameCombo;

    public StaticEntryDNWizardPage() {
        super(NAME);

        setDescription("Enter the DN and optionally the class name of the entry.");
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

        Label exampleDnLabel = new Label(composite, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        exampleDnLabel.setLayoutData(gd);
        exampleDnLabel.setText("Example: dc=Example,dc=com");

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);

        Label classLabel = new Label(composite, SWT.NONE);
        classLabel.setText("Class:");
        gd = new GridData();
        gd.widthHint = 50;
        classLabel.setLayoutData(gd);

        classNameCombo = new Combo(composite, SWT.BORDER);
        classNameCombo.add("");
        classNameCombo.add("org.safehaus.penrose.directory.DynamicEntry");
        classNameCombo.add("org.safehaus.penrose.directory.ProxyEntry");

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        classNameCombo.setLayoutData(gd);
        classNameCombo.addModifyListener(this);

        setPageComplete(validatePage());
    }

    public String getDn() {
        return dnText.getText();
    }

    public String getClassName() {
        return "".equals(classNameCombo.getText()) ? null : classNameCombo.getText();
    }

    public boolean validatePage() {
        return LDAPDN.isValid(getDn());
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
