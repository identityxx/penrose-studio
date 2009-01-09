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
import org.eclipse.swt.widgets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi S. Dewata
 */
public class EntryClassWizardPage extends WizardPage implements ModifyListener {

    Logger log = LoggerFactory.getLogger(getClass());

    public final static String NAME = "Entry Class";

    Combo classNameCombo;

    String entryClass;

    public EntryClassWizardPage() {
        super(NAME);
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label classLabel = new Label(composite, SWT.NONE);
        classLabel.setText("Class:");

        classLabel.setLayoutData(new GridData());

        classNameCombo = new Combo(composite, SWT.BORDER);
        classNameCombo.add("");
        classNameCombo.add("org.safehaus.penrose.directory.DynamicEntry");
        classNameCombo.add("org.safehaus.penrose.directory.ProxyEntry");

        if (entryClass != null) {
            classNameCombo.setText(entryClass);
        }

        classNameCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        classNameCombo.addModifyListener(this);

        setPageComplete(validatePage());
    }

    public String getClassName() {
        return "".equals(classNameCombo.getText()) ? null : classNameCombo.getText();
    }

    public boolean validatePage() {
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }

    public void setClassName(String entryClass) {
        this.entryClass = entryClass;
    }
}