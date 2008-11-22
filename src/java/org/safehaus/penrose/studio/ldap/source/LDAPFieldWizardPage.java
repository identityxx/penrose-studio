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
package org.safehaus.penrose.studio.ldap.source;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class LDAPFieldWizardPage extends WizardPage implements SelectionListener, MouseListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "LDAP Fields";

    Table fieldTable;

    public LDAPFieldWizardPage() {
        super(NAME);
        setDescription("Select primary key fields.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 1;
        composite.setLayout(sectionLayout);

        Label adapterLabel = new Label(composite, SWT.NONE);
        adapterLabel.setText("Fields:");

        fieldTable = new Table(composite, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
        fieldTable.setHeaderVisible(true);
        fieldTable.setLinesVisible(true);
        fieldTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        fieldTable.addMouseListener(this);

        TableColumn tc = new TableColumn(fieldTable, SWT.NONE);
        tc.setText("Field");
        tc.setWidth(300);

        //tc = new TableColumn(fieldTable, SWT.NONE);
        //tc.setText("Expression");
        //tc.setWidth(300);

        tc = new TableColumn(fieldTable, SWT.NONE);
        tc.setText("Type");
        tc.setWidth(100);

        setPageComplete(validatePage());
    }

    public void setAttributeTypes(Collection<AttributeType> attributeTypes) {
        setAttributeTypes(attributeTypes, new ArrayList<String>());
    }

    public void setAttributeTypes(Collection<AttributeType> attributeTypes, Collection<String> primaryKeyNames) {
        fieldTable.removeAll();

        for (AttributeType attributeType : attributeTypes) {
            String name = attributeType.getName();
            boolean pk = primaryKeyNames.contains(name.toLowerCase());

            FieldConfig field = new FieldConfig();
            field.setName(name);
            field.setPrimaryKey(pk);

            TableItem it = new TableItem(fieldTable, SWT.CHECK);
            it.setImage(PenroseStudioPlugin.getImage(pk ? PenroseImage.KEY : PenroseImage.NOKEY));
            it.setText(0, name);
            it.setText(1, field.getType());
            it.setChecked(field.isPrimaryKey());
            it.setData(field);
        }

        setPageComplete(validatePage());
    }

    public Collection<FieldConfig> getFields() {
        Collection<FieldConfig> fields = new ArrayList<FieldConfig>();
        TableItem items[] = fieldTable.getItems();
        for (TableItem item : items) {
            FieldConfig field = (FieldConfig) item.getData();
            fields.add(field);
        }
        return fields;
    }

    public boolean validatePage() {
        TableItem items[] = fieldTable.getItems();
        if (items.length == 0) return true;

        for (TableItem item : items) {
            if (item.getChecked()) return true;
        }

        return false;
    }

    public void updateImages() {
        TableItem items[] = fieldTable.getItems();
        for (TableItem item : items) {
            FieldConfig field = (FieldConfig) item.getData();
            field.setPrimaryKey(item.getChecked());
            item.setImage(PenroseStudioPlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
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
}
