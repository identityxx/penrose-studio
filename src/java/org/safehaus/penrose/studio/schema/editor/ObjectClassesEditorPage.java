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
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.schema.SchemaClient;
import org.safehaus.penrose.studio.schema.wizard.ObjectClassWizard;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.client.PenroseClient;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class ObjectClassesEditorPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table table;

    SchemaEditor editor;

    Server server;
    String schemaName;

    public ObjectClassesEditorPage(SchemaEditor editor) {
        super(editor, "Object Classes", "  Object Classes  ");

        this.editor = editor;
        this.server = editor.getServer();
        this.schemaName = editor.getSchema().getName();
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
        tc.setText("Names");
        tc.setWidth(200);

        tc = new TableColumn(table, SWT.LEFT);
        tc.setText("Description");
        tc.setWidth(200);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
		addButton.setText("Add");

        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ObjectClass objectClass = new ObjectClass();

                    ObjectClassWizard wizard = new ObjectClassWizard();
                    wizard.setWindowTitle("Add Object Class");
                    wizard.setServer(server);
                    wizard.setObjectClass(objectClass);

                    WizardDialog dialog = new WizardDialog(parent.getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    PenroseClient client = server.getClient();

                    SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
                    SchemaClient schemaClient = schemaManagerClient.getSchemaClient(schemaName);
                    schemaClient.addObjectClass(objectClass);
                    schemaClient.store();

                    refresh();

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
                    ObjectClass objectClass = (ObjectClass)ti.getData();

                    PenroseClient client = server.getClient();

                    SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
                    SchemaClient schemaClient = schemaManagerClient.getSchemaClient(schemaName);
                    schemaClient.removeObjectClass(objectClass.getName());
                    schemaClient.store();

                    refresh();

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
                    ObjectClass objectClass = (ObjectClass)ti.getData();

                    ObjectClassWizard wizard = new ObjectClassWizard();
                    wizard.setWindowTitle("Edit Object Class");
                    wizard.setServer(server);
                    wizard.setObjectClass(objectClass);

                    WizardDialog dialog = new WizardDialog(parent.getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    PenroseClient client = server.getClient();

                    SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
                    SchemaClient schemaClient = schemaManagerClient.getSchemaClient(schemaName);
                    schemaClient.updateObjectClass(objectClass.getName(), objectClass);
                    schemaClient.store();

                    refresh();

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

        try {
            PenroseClient client = server.getClient();

            SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
            SchemaClient schemaClient = schemaManagerClient.getSchemaClient(schemaName);

            log.debug("Object classes:");
            for (ObjectClass objectClass : schemaClient.getObjectClasses()) {
                log.debug(" - "+objectClass.getName());

                StringBuilder sb = new StringBuilder();
                for (String name : objectClass.getNames()) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(name);
                }

                TableItem item = new TableItem(table, SWT.NONE);
                item.setText(0, objectClass.getOid());
                item.setText(1, sb.toString());
                item.setText(2, objectClass.getDescription() == null ? "" : objectClass.getDescription());
                item.setData(objectClass);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}
