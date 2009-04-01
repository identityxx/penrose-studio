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
package org.safehaus.penrose.studio.source.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class SourcePrimaryKeysWizardPage extends WizardPage implements SelectionListener, MouseListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Primary Key Fields";

    Table fieldsTable;

    Map<String,FieldConfig> fieldConfigs;

    public SourcePrimaryKeysWizardPage() {
        super(NAME);
        setDescription("Select primary key fields.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout());

        Label adapterLabel = new Label(composite, SWT.NONE);
        adapterLabel.setText("Fields:");

        fieldsTable = new Table(composite, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
        fieldsTable.setHeaderVisible(true);
        fieldsTable.setLinesVisible(true);
        fieldsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        fieldsTable.addMouseListener(this);

        TableColumn tc = new TableColumn(fieldsTable, SWT.NONE);
        tc.setText("Field");
        tc.setWidth(300);

        //tc = new TableColumn(fieldTable, SWT.NONE);
        //tc.setText("Expression");
        //tc.setWidth(300);

        tc = new TableColumn(fieldsTable, SWT.NONE);
        tc.setText("Type");
        tc.setWidth(100);

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        TableItem items[] = fieldsTable.getItems();
        if (items.length == 0) return true;

        for (TableItem item : items) {
            if (item.getChecked()) return true;
        }

        return false;
    }

    public void updateImages() {
        TableItem items[] = fieldsTable.getItems();
        for (TableItem item : items) {
            FieldConfig field = (FieldConfig) item.getData();
            field.setPrimaryKey(item.getChecked());
            item.setImage(PenroseStudio.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
        }
    }

    public void widgetSelected(SelectionEvent event) {
        updateImages();
        setPageComplete(validatePage());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }

    public void mouseDoubleClick(MouseEvent event) {
    }

    public void mouseDown(MouseEvent event) {
    }

    public void mouseUp(MouseEvent event) {
        updateImages();
        setPageComplete(validatePage());
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
        fieldsTable.removeAll();

        for (FieldConfig fieldConfig : fieldConfigs.values()) {

            TableItem it = new TableItem(fieldsTable, SWT.CHECK);
            it.setImage(PenroseStudio.getImage(fieldConfig.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
            it.setText(0, fieldConfig.getName());
            it.setText(1, fieldConfig.getType());
            it.setChecked(fieldConfig.isPrimaryKey());
            it.setData(fieldConfig);
        }
    }

    public Map<String,FieldConfig> getFieldConfigs() {
        return fieldConfigs;
    }

    public void setFieldConfigs(Map<String,FieldConfig> fieldConfigs) {
        this.fieldConfigs = fieldConfigs;
    }
}