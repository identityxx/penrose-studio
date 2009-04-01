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
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.wizard.WizardPage;
import org.safehaus.penrose.schema.ObjectClass;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Endi S. Dewata
 */
public class ObjectClassPropertiesWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Properties";

    Text oidText;
    Text namesText;
    Text descriptionText;
    Combo typeCombo;
    Text superClassesText;
    Button obsoleteCheckbox;

    String oid;
    Collection<String> names = new ArrayList<String>();
    String description;
    String type;
    Collection<String> superClasses = new ArrayList<String>();
    boolean obsolete;

	public ObjectClassPropertiesWizardPage() {
		super(NAME);
        setDescription("Enter the object class properties.");
    }

    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);
        
        composite.setLayout(new GridLayout(2, false));

        Label oidLabel = new Label(composite, SWT.NONE);
        oidLabel.setText("OID:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        oidLabel.setLayoutData(gd);

        oidText = new Text(composite, SWT.BORDER);
        oidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        oidText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                oid = oidText.getText().trim();
                oid = "".equals(oid) ? null : oid;
            }
        });

        Label namesLabel = new Label(composite, SWT.NONE);
        namesLabel.setText("Names:");
        gd = new GridData();
        gd.widthHint = 100;
        namesLabel.setLayoutData(gd);

        namesText = new Text(composite, SWT.BORDER);
        namesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        namesText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                names.clear();
                StringTokenizer st = new StringTokenizer(namesText.getText());
                while (st.hasMoreTokens()) {
                    names.add(st.nextToken());
                }
            }
        });

        Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setText("Description:");
        gd = new GridData();
        gd.widthHint = 100;
        descriptionLabel.setLayoutData(gd);

        descriptionText = new Text(composite, SWT.BORDER);
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                description = descriptionText.getText().trim();
                description = "".equals(description) ? null : description;
            }
        });

        Label separatorLabel = new Label(composite, SWT.NONE);
        gd = new GridData();
        gd.horizontalSpan = 2;
        separatorLabel.setLayoutData(gd);

        Label typeLabel = new Label(composite, SWT.NONE);
        typeLabel.setText("Type:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        typeLabel.setLayoutData(gd);

        typeCombo = new Combo(composite, SWT.READ_ONLY);
        typeCombo.add(ObjectClass.STRUCTURAL);
        typeCombo.add(ObjectClass.ABSTRACT);
        typeCombo.add(ObjectClass.AUXILIARY);
        typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        typeCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                type = typeCombo.getText();
                type = "".equals(type) ? null : type;
            }
        });

        Label superClassesLabel = new Label(composite, SWT.NONE);
        superClassesLabel.setText("Super Classes:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        superClassesLabel.setLayoutData(gd);

        superClassesText = new Text(composite, SWT.BORDER);
        superClassesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        superClassesText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                superClasses.clear();
                StringTokenizer st = new StringTokenizer(superClassesText.getText());
                while (st.hasMoreTokens()) {
                    superClasses.add(st.nextToken());
                }
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
        oidText.setText(oid == null ? "" : oid);

        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(name);
        }
        namesText.setText(sb.toString());

        descriptionText.setText(description == null ? "" : description);

        typeCombo.setText(type == null ? "" : type);

        sb = new StringBuilder();
        for (String superClass : superClasses) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(superClass);
        }
        superClassesText.setText(sb.toString());

        obsoleteCheckbox.setSelection(obsolete);
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Collection<String> getNames() {
        return names;
    }

    public void setNames(Collection<String> names) {
        if (this.names == names) return;
        this.names.clear();
        this.names.addAll(names);
    }

    public String getObjectClassDescription() {
        return description;
    }

    public void setObjectClassDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<String> getSuperClasses() {
        return superClasses;
    }

    public void setSuperClasses(Collection<String> superClasses) {
        if (this.superClasses == superClasses) return;
        this.superClasses.clear();
        this.superClasses.addAll(superClasses);
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }
}