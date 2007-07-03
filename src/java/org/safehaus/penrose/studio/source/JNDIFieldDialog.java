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
package org.safehaus.penrose.studio.source;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.partition.FieldConfig;
import org.safehaus.penrose.schema.AttributeType;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class JNDIFieldDialog extends FieldDialog {

    Table attributeTypeTable;
    Map items = new HashMap();

	public JNDIFieldDialog(Shell parent, int style) {
		super(parent, style);
        setText("Field Editor");

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite selectorPage = createSelectorPage(tabFolder);
        selectorPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem selectorTab = new TabItem(tabFolder, SWT.NONE);
        selectorTab.setText("Attribute Type");
        selectorTab.setControl(selectorPage);

        Composite propertiesPage = createPropertiesPage(tabFolder);
        propertiesPage.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabItem propertiesTab = new TabItem(tabFolder, SWT.NONE);
        propertiesTab.setText("Properties");
        propertiesTab.setControl(propertiesPage);

        Composite buttons = getButtons(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        buttons.setLayoutData(gd);
    }

    public void save()  {
        if ("".equals(aliasText.getText())) return;
        if (attributeTypeTable.getSelectionCount() == 0) return;

        TableItem item = attributeTypeTable.getSelection()[0];
        if (item.getData() == null) return;

        AttributeType at = (AttributeType)item.getData();
        String originalName = at.getName();
        String alias = "".equals(aliasText.getText()) ? null : aliasText.getText();

        String type = typeCombo.getText();
        int length = "".equals(lengthText.getText()) ? FieldConfig.DEFAULT_LENGTH : Integer.parseInt(lengthText.getText());
        int precision = "".equals(precisionText.getText()) ? FieldConfig.DEFAULT_PRECISION : Integer.parseInt(precisionText.getText());

        fieldConfig.setName(alias);
        fieldConfig.setOriginalName(originalName);

        fieldConfig.setType(type);
        fieldConfig.setLength(length);
        fieldConfig.setPrecision(precision);
        fieldConfig.setUnique(uniqueCheckbox.getSelection());
        fieldConfig.setCaseSensitive(caseSensitiveCheckbox.getSelection());
    }

    public Composite createSelectorPage(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        attributeTypeTable = new Table(composite, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        attributeTypeTable.setLayoutData(gd);

        attributeTypeTable.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent event) {
                if (attributeTypeTable.getSelectionCount() == 0) return;

                TableItem item = attributeTypeTable.getSelection()[0];
                if (item.getData() != null) {
                    AttributeType at = (AttributeType)item.getData();
                    aliasText.setText(at.getName());

                    typeCombo.setText("VARCHAR");
                    lengthText.setText(50+"");
                    precisionText.setText("");
                }

                //aliasText.setEnabled(canEnterAlias());
                //saveButton.setEnabled(canSave());
            }
        });

        Label aliasLabel = new Label(composite, SWT.NONE);
        aliasLabel.setText("Alias:");
        gd = new GridData(GridData.FILL);
        aliasLabel.setLayoutData(gd);

        aliasText = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		aliasText.setLayoutData(gd);

        return composite;
    }

    public void setAttributeTypes(Collection<AttributeType> attributeTypes) {
        attributeTypeTable.removeAll();
        items.clear();

        for (Iterator i=attributeTypes.iterator(); i.hasNext(); ) {
            AttributeType attributeType = (AttributeType)i.next();
            TableItem ti = new TableItem(attributeTypeTable, SWT.NONE);
            ti.setText(attributeType.getName());
            ti.setData(attributeType);
            items.put(attributeType.getName(), ti);
        }
   }

    public void setFieldConfig(FieldConfig fc) {
        super.setFieldConfig(fc);

        String originalName = fc.getOriginalName() == null ? "" : fc.getOriginalName();

        TableItem item = (TableItem)items.get(originalName);
        if (item != null) {
            attributeTypeTable.setSelection(new TableItem[] { item });
        }
    }
}
