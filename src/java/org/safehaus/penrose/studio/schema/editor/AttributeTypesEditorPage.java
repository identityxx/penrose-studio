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
package org.safehaus.penrose.studio.schema.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.studio.schema.dialog.AttributeTypeDialog;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class AttributeTypesEditorPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table table;

    SchemaEditor editor;
    Schema schema;

    public AttributeTypesEditorPage(SchemaEditor editor) {
        super(editor, "Attribute Types", "  Attribute Types  ");

        this.editor = editor;
        this.schema = editor.getSchema();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Attribute Types");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Attribute Types");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control ocSection = createSection(section);
        section.setClient(ocSection);

        refresh();
    }

    public Composite createSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        table = toolkit.createTable(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(table, SWT.LEFT);
        tc.setText("OID");
        tc.setWidth(200);

        tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Name");
        tc.setWidth(150);

        tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Description");
        tc.setWidth(250);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
		addButton.setText("Add");

        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    AttributeType attributeType = new AttributeType();

                    AttributeTypeDialog dialog = new AttributeTypeDialog(parent.getShell(), SWT.NONE);
                    dialog.setAttributeType(attributeType);
                    dialog.open();

                    if (dialog.getAction() == AttributeTypeDialog.CANCEL) return;

                    schema.addAttributeType(attributeType);

                    refresh();
                    checkDirty();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText("Remove");

        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    TableItem ti = table.getSelection()[0];
                    AttributeType attributeType = (AttributeType)ti.getData();

                    schema.removeAttributeType(attributeType.getOid());

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    TableItem ti = table.getSelection()[0];
                    AttributeType attributeType = (AttributeType)ti.getData();

                    AttributeTypeDialog dialog = new AttributeTypeDialog(parent.getShell(), SWT.NONE);
                    dialog.setAttributeType(attributeType);
                    dialog.open();

                    if (dialog.getAction() == AttributeTypeDialog.CANCEL) return;

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public void refresh() {
        table.removeAll();

        Collection list = schema.getAttributeTypes();

        log.debug("Attribute type:");
        for (Iterator i=list.iterator(); i.hasNext(); ) {
            AttributeType attributeType = (AttributeType)i.next();
            log.debug(" - "+attributeType.getName());

            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, attributeType.getOid());
            item.setText(1, attributeType.getName());
            item.setText(2, attributeType.getDescription() == null ? "" : attributeType.getDescription());
            item.setData(attributeType);
        }
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}
