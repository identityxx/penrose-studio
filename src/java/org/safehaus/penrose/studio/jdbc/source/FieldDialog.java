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
package org.safehaus.penrose.studio.jdbc.source;

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
public class FieldDialog extends org.safehaus.penrose.studio.source.editor.FieldDialog {

    Table columnsTable;
    Map items = new HashMap();

	public FieldDialog(Shell parent, int style) {
		super(parent, style);
    }

    public Composite createFieldPage(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));

        columnsTable = new Table(composite, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        columnsTable.setLayoutData(gd);

        columnsTable.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent event) {
                if (columnsTable.getSelectionCount() == 0) return;

                TableItem item = columnsTable.getSelection()[0];
                if (item.getData() == null) return;

                FieldConfig fc = (FieldConfig)item.getData();
                getNameText().setText(fc.getName());

                getTypeCombo().setText(fc.getType());
                getLengthText().setText(fc.getLength()+"");
                getPrecisionText().setText(fc.getPrecision()+"");
            }
        });

        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        nameLabel.setLayoutData(gd);

        setNameText(new Text(composite, SWT.BORDER));
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
		getNameText().setLayoutData(gd);

        Label aliasLabel = new Label(composite, SWT.NONE);
        aliasLabel.setText("Alias:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 50;
        aliasLabel.setLayoutData(gd);

        setAliasText(new Text(composite, SWT.BORDER));
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
		getAliasText().setLayoutData(gd);

        return composite;
    }

    public void setColumns(Collection fields) {
        columnsTable.removeAll();
        items.clear();

        for (Iterator i=fields.iterator(); i.hasNext(); ) {
            FieldConfig fieldConfig = (FieldConfig)i.next();
            TableItem ti = new TableItem(columnsTable, SWT.NONE);
            ti.setText(fieldConfig.getName());
            ti.setData(fieldConfig);
            items.put(fieldConfig.getName(), ti);
        }
   }

    public void setFieldConfig(FieldConfig fieldConfig) {
        super.setFieldConfig(fieldConfig);

        String name = fieldConfig.getOriginalName();
        TableItem item = (TableItem)items.get(name);
        if (item != null) {
            columnsTable.setSelection(new TableItem[] { item });
        }
    }
}
