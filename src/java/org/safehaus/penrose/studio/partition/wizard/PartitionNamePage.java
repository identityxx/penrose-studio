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
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

/**
 * @author Endi S. Dewata
 */
public class PartitionNamePage extends WizardPage implements ModifyListener {

    public final static String NAME = "Partition Name";

    Text partitionNameText;

    private String partitionName;

    public PartitionNamePage() {
        super(NAME);
        setDescription("Enter the name of the partition.");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        nameLabel.setLayoutData(gd);

        partitionNameText = new Text(composite, SWT.BORDER);
        partitionNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        partitionNameText.addModifyListener(this);

        partitionNameText.setText(partitionName == null ? "" : partitionName);

        setPageComplete(validatePage());
    }

    public String getPartitionName() {
        return partitionNameText.getText().trim();
    }

    public boolean validatePage() {
        if ("".equals(getPartitionName())) return false;
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }
}
