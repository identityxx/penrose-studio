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

import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.source.editor.SourceEditorPage;
import org.safehaus.penrose.studio.source.editor.SourceEditor;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.ldap.LDAPClient;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.source.FieldConfig;

public class SourcePropertiesPage extends SourceEditorPage {

    Text sourceNameText;
    Combo connectionNameCombo;
    Text descriptionText;

    Text baseDnText;
    Text filterText;
    Combo scopeCombo;
    Text objectClassesText;

    Table fieldTable;

    Button addButton;
    Button editButton;
    Button removeButton;

    Schema schema;

    String[] scopes = new String[] { "OBJECT", "ONELEVEL", "SUBTREE" };

    public SourcePropertiesPage(SourceEditor editor) {
        super(editor, "PROPERTIES", "  Properties  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Properties");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control sourceSection = createPropertiesControl(section);
        section.setClient(sourceSection);

        section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("LDAP Properties");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control directorySection = createLDAPControl(section);
        section.setClient(directorySection);

        section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Fields");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control fieldsSection = createFieldsControl(section);
        section.setClient(fieldsSection);
    }

    public void refresh() {

        String sourceName = getSourceConfig().getName();
        sourceNameText.setText(sourceName == null ? "" : sourceName);

        String connectionName = getSourceConfig().getConnectionName();
        connectionNameCombo.setText(connectionName == null ? "" : connectionName);

        String description = getSourceConfig().getDescription();
        descriptionText.setText(description == null ? "" : description);

        String baseDn = getSourceConfig().getParameter("baseDn");
        baseDnText.setText(baseDn == null ? "" : baseDn);

        String filter = getSourceConfig().getParameter("filter");
        filterText.setText(filter == null ? "" : filter);

        String scope = getSourceConfig().getParameter("scope");
        scopeCombo.setText(scope == null ? "" : scope);

        String objectClasses = getSourceConfig().getParameter("objectClasses");
        objectClassesText.setText(objectClasses == null ? "" : objectClasses);

        fieldTable.removeAll();

        Collection fields = getSourceConfig().getFieldConfigs();
        Map map = new TreeMap();
        for (Iterator i=fields.iterator(); i.hasNext(); ) {
            FieldConfig fieldDefinition = (FieldConfig)i.next();
            map.put(fieldDefinition.getOriginalName(), fieldDefinition);
        }

        for (Iterator i=map.values().iterator(); i.hasNext(); ) {
            FieldConfig fieldConfig = (FieldConfig)i.next();

            TableItem item = new TableItem(fieldTable, SWT.CHECK);
            item.setChecked(fieldConfig.isPK());
            item.setImage(PenrosePlugin.getImage(fieldConfig.isPK() ? PenroseImage.KEY : PenroseImage.NOKEY));
            item.setText(0, fieldConfig.getName().equals(fieldConfig.getOriginalName()) ? fieldConfig.getName() : fieldConfig.getOriginalName());
            item.setText(1, fieldConfig.getName().equals(fieldConfig.getOriginalName()) ? "" : fieldConfig.getName());
            item.setText(2, fieldConfig.getType());
            item.setData(fieldConfig);
        }
    }

    public Composite createPropertiesControl(Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label sourceNameLabel = getToolkit().createLabel(composite, "Source Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        sourceNameLabel.setLayoutData(gd);

        sourceNameText = getToolkit().createText(composite, "", SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        sourceNameText.setLayoutData(gd);

        sourceNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getSourceConfig().setName("".equals(sourceNameText.getText()) ? null : sourceNameText.getText());
                checkDirty();
            }
        });

        Label connectionNameLabel = getToolkit().createLabel(composite, "Connection Name:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        connectionNameLabel.setLayoutData(gd);

        connectionNameCombo = new Combo(composite, SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        connectionNameCombo.setLayoutData(gd);

        for (Iterator i=getPartition().getConnectionConfigs().iterator(); i.hasNext(); ) {
            ConnectionConfig connectionConfig = (ConnectionConfig)i.next();
            connectionNameCombo.add(connectionConfig.getName());
        }

        connectionNameCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                getSourceConfig().setConnectionName("".equals(connectionNameCombo.getText()) ? null : connectionNameCombo.getText());
                checkDirty();
            }
        });

        Label descriptionLabel = getToolkit().createLabel(composite, "Description:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        descriptionLabel.setLayoutData(gd);

        descriptionText = getToolkit().createText(composite, "", SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        descriptionText.setLayoutData(gd);

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getSourceConfig().setDescription("".equals(descriptionText.getText()) ? null : descriptionText.getText());
                checkDirty();
            }
        });

        return composite;
    }

    public Composite createLDAPControl(Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label baseDnLabel = getToolkit().createLabel(composite, "Base DN:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        baseDnLabel.setLayoutData(gd);

        baseDnText = getToolkit().createText(composite, "", SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        baseDnText.setLayoutData(gd);

        baseDnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(baseDnText.getText())) {
                    getSourceConfig().removeParameter("baseDn");
                } else {
                    getSourceConfig().setParameter("baseDn", baseDnText.getText());
                }
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "Filter:");

        filterText = getToolkit().createText(composite, "", SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        filterText.setLayoutData(gd);

        filterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(filterText.getText())) {
                    getSourceConfig().removeParameter("filter");
                } else {
                    getSourceConfig().setParameter("filter", filterText.getText());
                }
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "Scope:");

        scopeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        for (int i=0; i<scopes.length; i++) {
            scopeCombo.add(scopes[i]);
        }
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        scopeCombo.setLayoutData(gd);

        scopeCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if ("".equals(scopeCombo.getText())) {
                    getSourceConfig().removeParameter("scope");
                } else {
                    getSourceConfig().setParameter("scope", scopeCombo.getText());
                }
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "Object Classes:");

        objectClassesText = getToolkit().createText(composite, "", SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        objectClassesText.setLayoutData(gd);

        objectClassesText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(objectClassesText.getText())) {
                    getSourceConfig().removeParameter("objectClasses");
                } else {
                    getSourceConfig().setParameter("objectClasses", objectClassesText.getText());
                }
                checkDirty();
            }
        });

        return composite;
    }

    public Composite createFieldsControl(final Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        fieldTable = getToolkit().createTable(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK | SWT.MULTI);
        fieldTable.setHeaderVisible(true);
        fieldTable.setLinesVisible(true);

        fieldTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        fieldTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    if (fieldTable.getSelectionCount() == 0) return;

                    int index = fieldTable.getSelectionIndex();
                    TableItem item = fieldTable.getSelection()[0];
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    String oldName = fieldDefinition.getName();

                    if (schema == null) {
                        ConnectionConfig connectionConfig = getPartition().getConnectionConfig(getSourceConfig().getConnectionName());
                        LDAPClient client = new LDAPClient(connectionConfig.getParameters());

                        schema = client.getSchema();
                    }

                    Collection attributeTypes = schema.getAttributeTypes();

                    FieldDialog dialog = new FieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setAttributeTypes(attributeTypes);
                    dialog.setFieldConfig(fieldDefinition);
                    dialog.open();

                    if (dialog.getAction() == org.safehaus.penrose.studio.source.editor.FieldDialog.CANCEL) return;

                    String newName = fieldDefinition.getName();

                    if (!oldName.equals(newName)) {
                        getSourceConfig().renameFieldConfig(oldName, newName);
                    }

                    refresh();
                    fieldTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }

            public void mouseUp(MouseEvent e) {
                for (int i=0; i<fieldTable.getItemCount(); i++) {
                    TableItem item = fieldTable.getItem(i);
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    fieldDefinition.setPrimaryKey(item.getChecked()+"");
                    item.setImage(PenrosePlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
                }

                checkDirty();
            }
        });

        fieldTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                for (int i=0; i<fieldTable.getItemCount(); i++) {
                    TableItem item = fieldTable.getItem(i);
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    fieldDefinition.setPrimaryKey(item.getChecked()+"");
                    item.setImage(PenrosePlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
                }

                checkDirty();
            }
        });

        TableColumn tc = new TableColumn(fieldTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(250);

        tc = new TableColumn(fieldTable, SWT.NONE);
        tc.setText("Alias");
        tc.setWidth(250);

        tc = new TableColumn(fieldTable, SWT.NONE);
        tc.setText("Type");
        tc.setWidth(100);

        Composite buttons = getToolkit().createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        addButton = getToolkit().createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    FieldConfig fieldDefinition = new FieldConfig();

                    Server server = getServer();
                    SchemaManager schemaManager = server.getSchemaManager();
                    Collection attributeTypes = schemaManager.getAttributeTypes();

                    FieldDialog dialog = new FieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setAttributeTypes(attributeTypes);
                    dialog.setFieldConfig(fieldDefinition);
                    dialog.open();

                    if (dialog.getAction() == org.safehaus.penrose.studio.source.editor.FieldDialog.CANCEL) return;

                    getSourceConfig().addFieldConfig(fieldDefinition);

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        editButton = getToolkit().createButton(buttons, "Edit", SWT.PUSH);
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (fieldTable.getSelectionCount() == 0) return;

                    int index = fieldTable.getSelectionIndex();
                    TableItem item = fieldTable.getSelection()[0];
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    String oldName = fieldDefinition.getName();

                    Server server = getServer();
                    SchemaManager schemaManager = server.getSchemaManager();
                    Collection attributeTypes = schemaManager.getAttributeTypes();

                    FieldDialog dialog = new FieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setAttributeTypes(attributeTypes);
                    dialog.setFieldConfig(fieldDefinition);
                    dialog.open();

                    if (dialog.getAction() == org.safehaus.penrose.studio.source.editor.FieldDialog.CANCEL) return;

                    String newName = fieldDefinition.getName();

                    if (!oldName.equals(newName)) {
                        getSourceConfig().renameFieldConfig(oldName, newName);
                    }

                    refresh();
                    fieldTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        removeButton = getToolkit().createButton(buttons, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (fieldTable.getSelectionCount() == 0) return;

                    TableItem items[] = fieldTable.getSelection();
                    for (int i=0; i<items.length; i++) {
                        FieldConfig fieldDefinition = (FieldConfig)items[i].getData();
                        getSourceConfig().removeFieldConfig(fieldDefinition);
                    }

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        return composite;
    }
}
