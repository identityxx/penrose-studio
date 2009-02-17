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
package org.safehaus.penrose.studio.ldap.connection.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.studio.connection.SchemaExportWizard;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.ldap.connection.LDAPConnectionClient;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class LDAPConnectionSchemaPage extends ConnectionEditorPage {

    Table objectClassesTable;
    Table attributeTypesTable;

    LDAPConnectionClient connectionClient;
    Schema schema;

    public LDAPConnectionSchemaPage(LDAPConnectionEditor editor) throws Exception {
        super(editor, "SCHEMA", "Schema");

        connectionClient = new LDAPConnectionClient(
                editor.getServer().getClient(),
                partitionName,
                editor.getConnectionName()
        );
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Actions");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control actionsSection = createActionsSection(section);
        section.setClient(actionsSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Object Classes");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control objectClassesSection = createObjectClassesSection(section);
        section.setClient(objectClassesSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Attribute Types");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control attributeTypesSection = createAttributeTypesSection(section);
        section.setClient(attributeTypesSection);

        update();
    }

    public Composite createActionsSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new RowLayout());

        Hyperlink refreshSchema = toolkit.createHyperlink(composite, "Refresh", SWT.NONE);

        refreshSchema.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                update();
            }
        });

        Hyperlink exportSchema = toolkit.createHyperlink(composite, "Export", SWT.NONE);

        exportSchema.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                try {
                    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

                    Schema newSchema = (Schema)schema.clone();

                    SchemaExportWizard wizard = new SchemaExportWizard(server, newSchema);
                    WizardDialog dialog = new WizardDialog(shell, wizard);
                    dialog.setPageSize(600, 300);
                    int rc = dialog.open();

                    if (rc == Window.CANCEL) return;


                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public Composite createAttributeTypesSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        attributeTypesTable = new Table(composite, SWT.BORDER | SWT.MULTI  | SWT.FULL_SELECTION);
        attributeTypesTable.setHeaderVisible(true);
        attributeTypesTable.setLinesVisible(true);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        attributeTypesTable.setLayoutData(gd);

        TableColumn tc = new TableColumn(attributeTypesTable, SWT.LEFT);
        tc.setText("Name");
        tc.setWidth(200);

        tc = new TableColumn(attributeTypesTable, SWT.LEFT);
        tc.setText("Description");
        tc.setWidth(350);

        return composite;
    }

    public Composite createObjectClassesSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        objectClassesTable = toolkit.createTable(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        objectClassesTable.setHeaderVisible(true);
        objectClassesTable.setLinesVisible(true);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        objectClassesTable.setLayoutData(gd);

        TableColumn tc = new TableColumn(objectClassesTable, SWT.LEFT);
        tc.setText("Name");
        tc.setWidth(200);

        tc = new TableColumn(objectClassesTable, SWT.LEFT);
        tc.setText("Description");
        tc.setWidth(350);

        return composite;
    }

    public void update() {
        try {
            attributeTypesTable.removeAll();
            objectClassesTable.removeAll();

            schema = connectionClient.getSchema();

            Collection<AttributeType> attributeTypes = schema.getAttributeTypes();
            for (AttributeType at : attributeTypes) {
                TableItem item = new TableItem(attributeTypesTable, SWT.NONE);
                item.setText(0, at.getName());
                item.setText(1, at.getDescription() == null ? "" : at.getDescription());
                item.setData(at);
            }

            Collection<ObjectClass> objectClasses = schema.getObjectClasses();
            for (ObjectClass oc : objectClasses) {
                TableItem item = new TableItem(objectClassesTable, SWT.NONE);
                item.setText(0, oc.getName());
                item.setText(1, oc.getDescription() == null ? "" : oc.getDescription());
                item.setData(oc);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}