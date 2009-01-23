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
package org.safehaus.penrose.studio.connection.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

/**
 * @author Endi S. Dewata
 */
public class ConnectionAdapterWizardPage extends WizardPage implements ModifyListener {

    public final static String NAME = "Connection Properties";

    Combo adapterCombo;

    private String adapter;

    public ConnectionAdapterWizardPage() {
        super(NAME);
        setDescription("Enter the adapter of the connection.");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label adapterLabel = new Label(composite, SWT.NONE);
        adapterLabel.setText("Adater:");
        adapterLabel.setLayoutData(new GridData());

        adapterCombo = new Combo(composite, SWT.BORDER);
        adapterCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        adapterCombo.addModifyListener(this);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if (adapterCombo.getText().equals("")) return false;
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
        adapterCombo.setText(adapter == null ? "" : adapter);
    }

    public String getAdapter() {
        String adapter = adapterCombo.getText();
        return "".equals(adapter) ? null : adapter;
    }

    public void setAdapter(String adapter) {
        this.adapter = adapter;
    }
}