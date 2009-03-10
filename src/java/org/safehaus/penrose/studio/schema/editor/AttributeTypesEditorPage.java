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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.schema.SchemaClient;
import org.safehaus.penrose.studio.schema.wizard.AttributeTypeWizard;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.client.PenroseClient;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class AttributeTypesEditorPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table table;

    SchemaEditor editor;

    Server server;
    String schemaName;

    public AttributeTypesEditorPage(SchemaEditor editor) {
        super(editor, "Attribute Types", "  Attribute Types  ");

        this.editor = editor;
        this.server = editor.getServer();
        this.schemaName = editor.getSchema().getName();
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
                    AttributeType attributeType = new AttributeType();

                    AttributeTypeWizard wizard = new AttributeTypeWizard();
                    wizard.setWindowTitle("Add Attribute Type");
                    wizard.setAttributeType(attributeType);

                    WizardDialog dialog = new WizardDialog(parent.getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    PenroseClient client = server.getClient();

                    SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
                    SchemaClient schemaClient = schemaManagerClient.getSchemaClient(schemaName);
                    schemaClient.addAttributeType(attributeType);
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
                    AttributeType attributeType = (AttributeType)ti.getData();

                    PenroseClient client = server.getClient();

                    SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
                    SchemaClient schemaClient = schemaManagerClient.getSchemaClient(schemaName);
                    schemaClient.removeAttributeType(attributeType.getName());
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
                    AttributeType attributeType = (AttributeType)ti.getData();

                    AttributeTypeWizard wizard = new AttributeTypeWizard();
                    wizard.setWindowTitle("Edit Attribute Type");
                    wizard.setAttributeType(attributeType);

                    WizardDialog dialog = new WizardDialog(parent.getShell(), wizard);
                    dialog.setPageSize(600, 300);
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;

                    PenroseClient client = server.getClient();

                    SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
                    SchemaClient schemaClient = schemaManagerClient.getSchemaClient(schemaName);
                    schemaClient.updateAttributeType(attributeType.getName(), attributeType);
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

            log.debug("Attribute type:");
            for (AttributeType attributeType : schemaClient.getAttributeTypes()) {
                log.debug(" - "+attributeType.getName());

                StringBuilder sb = new StringBuilder();
                for (String name : attributeType.getNames()) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(name);
                }

                TableItem item = new TableItem(table, SWT.NONE);
                item.setText(0, attributeType.getOid());
                item.setText(1, sb.toString());
                item.setText(2, attributeType.getDescription() == null ? "" : attributeType.getDescription());
                item.setData(attributeType);
            }
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}
