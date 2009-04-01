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
package org.safehaus.penrose.studio.attribute.wizard;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.wizard.WizardPage;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class AttributeWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Attribute";

    Combo objectClassCombo;
    Table attributesTable;
    Text attributeText;

    Schema schema;
    String attributeName;

    public AttributeWizardPage() {
        super(NAME);
        setDescription("Select or enter attribute name.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label objectClassLabel = new Label(composite, SWT.NONE);
        objectClassLabel.setText("Object Class:");
        objectClassLabel.setLayoutData(new GridData(GridData.FILL));

        objectClassCombo = new Combo(composite, SWT.READ_ONLY);
        objectClassCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        objectClassCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    String ocName = objectClassCombo.getText();
                    showAttributes(ocName);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        attributesTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        attributesTable.setHeaderVisible(true);
        attributesTable.setLinesVisible(true);

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        attributesTable.setLayoutData(gd);

        attributesTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (attributesTable.getSelectionCount() == 0) return;

                    TableItem item = attributesTable.getSelection()[0];
                    attributeName = item.getText().trim();
                    attributeText.setText(attributeName);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        TableColumn tc = new TableColumn(attributesTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(300);

        tc = new TableColumn(attributesTable, SWT.NONE);
        tc.setText("Required");
        tc.setWidth(100);

        Label attributeLabel = new Label(composite, SWT.NONE);
        attributeLabel.setText("Attribute:");
        attributeLabel.setLayoutData(new GridData(GridData.FILL));

        attributeText = new Text(composite, SWT.BORDER);
        attributeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        attributeText.setText(attributeName == null ? "" : attributeName);

        attributeText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                attributeName = attributeText.getText().trim();
                attributeName = "".equals(attributeName) ? null : attributeName;
            }
        });


        objectClassCombo.add("");

        for (String ocName : schema.getObjectClassNames()) {
            objectClassCombo.add(ocName);
        }

        showAttributes("");
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) throws Exception {
        this.schema = schema;
    }

    public boolean validatePage() {
        return getAttributeName() != null;
    }

    public void showAttributes(String ocName) {
        try {
            attributesTable.removeAll();

            if ("".equals(ocName)) {

                for (String atName : schema.getAttributeTypeNames()) {
                    TableItem item = new TableItem(attributesTable, SWT.NONE);
                    item.setText(0, atName);
                    item.setText(1, "");
                }

                return;
            }

            ObjectClass oc = schema.getObjectClass(ocName);
            Collection<String> atNames = oc.getRequiredAttributes();

            for (String atName : atNames) {
                TableItem item = new TableItem(attributesTable, SWT.NONE);
                item.setText(0, atName);
                item.setText(1, "Yes");
            }

            atNames = oc.getOptionalAttributes();

            for (String atName : atNames) {
                TableItem item = new TableItem(attributesTable, SWT.NONE);
                item.setText(0, atName);
                item.setText(1, "");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
}