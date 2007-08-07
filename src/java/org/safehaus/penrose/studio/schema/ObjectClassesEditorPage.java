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
package org.safehaus.penrose.studio.schema;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.Schema;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class ObjectClassesEditorPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table table;

    SchemaEditor editor;
    Schema schema;

    public ObjectClassesEditorPage(SchemaEditor editor) {
        super(editor, "Object Classes", "  Object Classes  ");

        this.editor = editor;
        this.schema = editor.getSchema();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Object Classes");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Object Classes");
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

        Button addButton = toolkit.createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ObjectClass objectClass = new ObjectClass();

                    ObjectClassDialog dialog = new ObjectClassDialog(parent.getShell(), SWT.NONE);
                    dialog.setObjectClass(objectClass);
                    dialog.open();

                    if (dialog.getAction() == ObjectClassDialog.CANCEL) return;

                    schema.addObjectClass(objectClass);

                    refresh();
                    checkDirty();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Button removeButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (table.getSelectionCount() == 0) return;

                    TableItem ti = table.getSelection()[0];
                    ObjectClass objectClass = (ObjectClass)ti.getData();

                    schema.removeObjectClass(objectClass.getOid());

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    TableItem ti = table.getSelection()[0];
                    ObjectClass objectClass = (ObjectClass)ti.getData();

                    ObjectClassDialog dialog = new ObjectClassDialog(parent.getShell(), SWT.NONE);
                    dialog.setObjectClass(objectClass);
                    dialog.open();

                    if (dialog.getAction() == ObjectClassDialog.CANCEL) return;

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public void refresh() {
        table.removeAll();

        Collection list = schema.getObjectClasses();

        for (Iterator i=list.iterator(); i.hasNext(); ) {
            ObjectClass objectClass = (ObjectClass)i.next();

            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, objectClass.getOid());
            item.setText(1, objectClass.getName());
            item.setText(2, objectClass.getDescription() == null ? "" : objectClass.getDescription());
            item.setData(objectClass);
        }
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}
