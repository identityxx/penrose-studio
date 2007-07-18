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
import org.safehaus.penrose.source.FieldConfig;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class JDBCFieldDialog extends FieldDialog {

    Table columnTable;
    Map items = new HashMap();

	public JDBCFieldDialog(Shell parent, int style) {
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
        selectorTab.setText("Column");
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
        if (columnTable.getSelectionCount() == 0) return;

        TableItem item = columnTable.getSelection()[0];
        if (item.getData() == null) return;

        FieldConfig fc = (FieldConfig)item.getData();
        String originalName = fc.getName();
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

        columnTable = new Table(composite, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        columnTable.setLayoutData(gd);

        columnTable.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent event) {
                if (columnTable.getSelectionCount() == 0) return;

                TableItem item = columnTable.getSelection()[0];
                if (item.getData() != null) {
                    FieldConfig fc = (FieldConfig)item.getData();
                    aliasText.setText(fc.getName());

                    typeCombo.setText(fc.getType());
                    lengthText.setText(fc.getLength()+"");
                    precisionText.setText(fc.getPrecision()+"");
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

    public void setColumns(Collection fields) {
        columnTable.removeAll();
        items.clear();

        for (Iterator i=fields.iterator(); i.hasNext(); ) {
            FieldConfig fieldConfig = (FieldConfig)i.next();
            TableItem ti = new TableItem(columnTable, SWT.NONE);
            ti.setText(fieldConfig.getName());
            ti.setData(fieldConfig);
            items.put(fieldConfig.getName(), ti);
        }
   }

    public void setFieldConfig(FieldConfig fc) {
        super.setFieldConfig(fc);

        String originalName = fc.getOriginalName() == null ? "" : fc.getOriginalName();

        TableItem item = (TableItem)items.get(originalName);
        if (item != null) {
            columnTable.setSelection(new TableItem[] { item });
        }
    }
}
