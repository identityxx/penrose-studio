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
package org.safehaus.penrose.studio.schema.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.wizard.WizardPage;
import org.safehaus.penrose.schema.AttributeType;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class AttributeTypeOptionsWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Options";

    Combo usageCombo;
    Button singleValuedCheckbox;
    Button collectiveCheckbox;
    Button modifiableCheckbox;
    Button obsoleteCheckbox;

    String usage;
    boolean singleValued;
    boolean collective;
    boolean modifiable;
    boolean obsolete;

    public AttributeTypeOptionsWizardPage() {
        super(NAME);
        setDescription("Enter the attribute type properties.");
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label usageLabel = new Label(composite, SWT.NONE);
        usageLabel.setText("Usage:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        usageLabel.setLayoutData(gd);

        usageCombo = new Combo(composite, SWT.READ_ONLY);
        usageCombo.add(AttributeType.USER_APPLICATIONS);
        usageCombo.add(AttributeType.DIRECTORY_OPERATION);
        usageCombo.add(AttributeType.DISTRIBUTED_OPERATION);
        usageCombo.add(AttributeType.DSA_OPERATION);
        usageCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        usageCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                usage = usageCombo.getText();
                usage = "".equals(usage) ? null : usage;
            }
        });

        Label singleValuedLabel = new Label(composite, SWT.NONE);
        singleValuedLabel.setText("Single-valued:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        singleValuedLabel.setLayoutData(gd);

        singleValuedCheckbox = new Button(composite, SWT.CHECK);
        singleValuedCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        singleValuedCheckbox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                singleValued = singleValuedCheckbox.getSelection();
            }
        });

        Label collectiveLabel = new Label(composite, SWT.NONE);
        collectiveLabel.setText("Collective:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        collectiveLabel.setLayoutData(gd);

        collectiveCheckbox = new Button(composite, SWT.CHECK);
        collectiveCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        collectiveCheckbox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                collective = collectiveCheckbox.getSelection();
            }
        });

        Label modifiableLabel = new Label(composite, SWT.NONE);
        modifiableLabel.setText("Modifiable:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        modifiableLabel.setLayoutData(gd);

        modifiableCheckbox = new Button(composite, SWT.CHECK);
        modifiableCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        modifiableCheckbox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                modifiable = modifiableCheckbox.getSelection();
            }
        });

        Label obsoleteLabel = new Label(composite, SWT.NONE);
        obsoleteLabel.setText("Obsolete:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        obsoleteLabel.setLayoutData(gd);

        obsoleteCheckbox = new Button(composite, SWT.CHECK);
        obsoleteCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        obsoleteCheckbox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                obsolete = obsoleteCheckbox.getSelection();
            }
        });
	}

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
        usageCombo.setText(usage == null ? "" : usage);
        singleValuedCheckbox.setSelection(singleValued);
        collectiveCheckbox.setSelection(collective);
        modifiableCheckbox.setSelection(modifiable);
        obsoleteCheckbox.setSelection(obsolete);
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public boolean isSingleValued() {
        return singleValued;
    }

    public void setSingleValued(boolean singleValued) {
        this.singleValued = singleValued;
    }

    public boolean isCollective() {
        return collective;
    }

    public void setCollective(boolean collective) {
        this.collective = collective;
    }

    public boolean isModifiable() {
        return modifiable;
    }

    public void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }
}