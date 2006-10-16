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
package org.safehaus.penrose.studio.connection;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.partition.ConnectionConfig;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.ldap.LDAPClient;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * @author Endi S. Dewata
 */
public class JNDIConnectionSchemaPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table objectClassesTable;
    Table attributeTypesTable;

    JNDIConnectionEditor editor;
    Partition partition;
    ConnectionConfig connectionConfig;

    Schema schema;

    public JNDIConnectionSchemaPage(JNDIConnectionEditor editor) {
        super(editor, "SCHEMA", "  Schema  ");

        this.editor = editor;
        this.partition = editor.getPartition();
        this.connectionConfig = editor.getConnectionConfig();
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Schema");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
/*
        if (penroseApplication.isFreeware()) {
            Label label = toolkit.createLabel(body, PenroseApplication.FEATURE_NOT_AVAILABLE);
            label.setLayoutData(new GridData(GridData.FILL_BOTH));
            return;
        }
*/
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

        refresh();
    }

    public Composite createActionsSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new RowLayout());

        Hyperlink refreshSchema = toolkit.createHyperlink(composite, "Refresh", SWT.NONE);

        refreshSchema.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                refresh();
            }
        });

        Hyperlink exportSchema = toolkit.createHyperlink(composite, "Export", SWT.NONE);

        exportSchema.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent event) {
                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

                Schema newSchema = (Schema)schema.clone();

                SchemaExportWizard wizard = new SchemaExportWizard(newSchema);
                WizardDialog dialog = new WizardDialog(shell, wizard);
                dialog.setPageSize(600, 300);
                dialog.open();
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

    public void refresh() {
        try {
            attributeTypesTable.removeAll();
            objectClassesTable.removeAll();

            LDAPClient client = new LDAPClient(connectionConfig.getParameters());
            schema = client.getSchema();

            Collection attributeTypes = schema.getAttributeTypes();
            for (Iterator i=attributeTypes.iterator(); i.hasNext(); ) {
                AttributeType at = (AttributeType)i.next();

                TableItem item = new TableItem(attributeTypesTable, SWT.NONE);
                item.setText(0, at.getName());
                item.setText(1, at.getDescription() == null ? "" : at.getDescription());
                item.setData(at);
            }

            Collection objectClasses = schema.getObjectClasses();
            for (Iterator i=objectClasses.iterator(); i.hasNext(); ) {
                ObjectClass oc = (ObjectClass)i.next();

                TableItem item = new TableItem(objectClassesTable, SWT.NONE);
                item.setText(0, oc.getName());
                item.setText(1, oc.getDescription() == null ? "" : oc.getDescription());
                item.setData(oc);
            }
        } catch (Exception e) {
            log.debug(e.getMessage(), e);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String message = sw.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }
            MessageDialog.openError(getEditorSite().getShell(), "Error", "Error: "+message);
        }
    }
}