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
package org.safehaus.penrose.studio.jndi.source;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.ldap.LDAPClient;
import org.safehaus.penrose.management.*;
import org.safehaus.penrose.management.schema.SchemaManagerClient;
import org.safehaus.penrose.management.directory.EntryClient;
import org.safehaus.penrose.management.connection.ConnectionClient;
import org.safehaus.penrose.management.partition.PartitionClient;
import org.safehaus.penrose.management.partition.PartitionManagerClient;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.SchemaUtil;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.source.FieldDialog;
import org.safehaus.penrose.studio.source.editor.SourceEditorPage;

import java.util.ArrayList;
import java.util.Collection;

public class JNDISourcePropertyPage extends SourceEditorPage {

	Text sourceNameText;
    Combo connectionNameCombo;

	Text baseDnText;
	Text filterText;
	Combo scopeCombo;
    Text objectClassesText;

	Table fieldTable;
	
    Button addButton;
    Button editButton;
    Button removeButton;

	String[] scopes = new String[] { "OBJECT", "ONELEVEL", "SUBTREE" };

    public JNDISourcePropertyPage(JNDISourceEditor editor) throws Exception {
        super(editor, "PROPERTIES", "  Properties  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Source Name");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control sourceSection = createSourceSection(section);
        section.setClient(sourceSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Directory Info");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control directorySection = createDirectorySection(section);
        section.setClient(directorySection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Fields");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control fieldsSection = createFieldsSection(section);
        section.setClient(fieldsSection);

        refresh();
	}

	public Composite createSourceSection(Composite parent) {

		Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(2, false));

		Label sourceNameLabel = toolkit.createLabel(composite, "Source Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        sourceNameLabel.setLayoutData(gd);

        sourceNameText = toolkit.createText(composite, sourceConfig.getName(), SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
		sourceNameText.setLayoutData(gd);

        sourceNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                sourceConfig.setName(sourceNameText.getText());
                checkDirty();
            }
        });

		toolkit.createLabel(composite, "Connection Name:");

        connectionNameCombo = new Combo(composite, SWT.READ_ONLY);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
		connectionNameCombo.setLayoutData(gd);

        try {
            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

            for (String connectionName : partitionClient.getConnectionNames()) {
                connectionNameCombo.add(connectionName);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        connectionNameCombo.setText(sourceConfig.getConnectionName());

        connectionNameCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                sourceConfig.setConnectionName(connectionNameCombo.getText());
                checkDirty();
            }
        });

        return composite;
	}
	
	public Composite createDirectorySection(Composite parent) {

		Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(2, false));

		Label baseDnLabel = toolkit.createLabel(composite, "Base DN:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        baseDnLabel.setLayoutData(gd);

        String s = sourceConfig.getParameter("baseDn");
		baseDnText = toolkit.createText(composite, s == null ? "" : s, SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        baseDnText.setLayoutData(gd);

        baseDnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(baseDnText.getText())) {
                    sourceConfig.removeParameter("baseDn");
                } else {
                    sourceConfig.setParameter("baseDn", baseDnText.getText());
                }
                checkDirty();
            }
        });

		toolkit.createLabel(composite, "Filter:");

        s = sourceConfig.getParameter("filter");
		filterText = toolkit.createText(composite, s == null ? "" : s, SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        filterText.setLayoutData(gd);

        filterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(filterText.getText())) {
                    sourceConfig.removeParameter("filter");
                } else {
                    sourceConfig.setParameter("filter", filterText.getText());
                }
                checkDirty();
            }
        });

		toolkit.createLabel(composite, "Scope:");

        s = sourceConfig.getParameter("scope");
		scopeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		for (int i=0; i<scopes.length; i++) {
			scopeCombo.add(scopes[i]);
            if (scopes[i].equals(s)) {
                scopeCombo.select(i);
            }
		}
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        scopeCombo.setLayoutData(gd);

        scopeCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if ("".equals(scopeCombo.getText())) {
                    sourceConfig.removeParameter("scope");
                } else {
                    sourceConfig.setParameter("scope", scopeCombo.getText());
                }
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Object Classes:");

        s = sourceConfig.getParameter("objectClasses");
        objectClassesText = toolkit.createText(composite, s == null ? "" : s, SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        objectClassesText.setLayoutData(gd);

        objectClassesText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(objectClassesText.getText())) {
                    sourceConfig.removeParameter("objectClasses");
                } else {
                    sourceConfig.setParameter("objectClasses", objectClassesText.getText());
                }
                checkDirty();
            }
        });

        return composite;
    }

    public Composite createFieldsSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        fieldTable = toolkit.createTable(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK | SWT.MULTI);
        fieldTable.setHeaderVisible(true);
        fieldTable.setLinesVisible(true);

        fieldTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		fieldTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                LDAPClient ldapClient = null;
                try {
                    if (fieldTable.getSelectionCount() == 0) return;

                    int index = fieldTable.getSelectionIndex();
                    TableItem item = fieldTable.getSelection()[0];
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    String oldName = fieldDefinition.getName();

                    Collection<AttributeType> attributeTypes;

                    PenroseClient client = project.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

                    ConnectionClient connectionClient = partitionClient.getConnectionClient(sourceConfig.getConnectionName());
                    ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();
                    
                    if (connectionConfig != null) {
                        ldapClient = new LDAPClient(connectionConfig.getParameters());
                    }
                    if (ldapClient == null) {
                        attributeTypes = new ArrayList<AttributeType>();
                    } else {

                        SchemaUtil schemaUtil = new SchemaUtil();
                        Schema schema = schemaUtil.getSchema(ldapClient);
                        attributeTypes = schema.getAttributeTypes();
                    }

                    JNDIFieldDialog dialog = new JNDIFieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setAttributeTypes(attributeTypes);
                    dialog.setFieldConfig(fieldDefinition);
                    dialog.open();

                    if (dialog.getAction() == FieldDialog.CANCEL) return;

                    String newName = fieldDefinition.getName();

                    if (!oldName.equals(newName)) {
                        sourceConfig.renameFieldConfig(oldName, newName);
                    }

                    refresh();
                    fieldTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);

                } finally {
                    if (ldapClient != null) try { ldapClient.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
                }
            }

			public void mouseUp(MouseEvent e) {
				for (int i=0; i<fieldTable.getItemCount(); i++) {
					TableItem item = fieldTable.getItem(i);
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    fieldDefinition.setPrimaryKey(item.getChecked());
					item.setImage(PenroseStudioPlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
				}

                checkDirty();
			}
		});

		fieldTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (int i=0; i<fieldTable.getItemCount(); i++) {
					TableItem item = fieldTable.getItem(i);
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    fieldDefinition.setPrimaryKey(item.getChecked());
					item.setImage(PenroseStudioPlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
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

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        addButton = toolkit.createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    FieldConfig fieldDefinition = new FieldConfig();

                    PenroseClient client = project.getClient();
                    SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();

                    Collection<AttributeType> attributeTypes = schemaManagerClient.getAttributeTypes();

                    JNDIFieldDialog dialog = new JNDIFieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setAttributeTypes(attributeTypes);
                    dialog.setFieldConfig(fieldDefinition);
                    dialog.open();

                    if (dialog.getAction() == FieldDialog.CANCEL) return;

                    sourceConfig.addFieldConfig(fieldDefinition);

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        editButton = toolkit.createButton(buttons, "Edit", SWT.PUSH);
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (fieldTable.getSelectionCount() == 0) return;

                    int index = fieldTable.getSelectionIndex();
                    TableItem item = fieldTable.getSelection()[0];
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    String oldName = fieldDefinition.getName();

                    PenroseClient client = project.getClient();
                    SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
                    Collection<AttributeType> attributeTypes = schemaManagerClient.getAttributeTypes();

                    JNDIFieldDialog dialog = new JNDIFieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setAttributeTypes(attributeTypes);
                    dialog.setFieldConfig(fieldDefinition);
                    dialog.open();

                    if (dialog.getAction() == FieldDialog.CANCEL) return;

                    String newName = fieldDefinition.getName();

                    if (!oldName.equals(newName)) {
                        sourceConfig.renameFieldConfig(oldName, newName);
                    }

                    refresh();
                    fieldTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        removeButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (fieldTable.getSelectionCount() == 0) return;

                    TableItem items[] = fieldTable.getSelection();
                    for (TableItem item : items) {
                        FieldConfig fieldDefinition = (FieldConfig) item.getData();
                        sourceConfig.removeFieldConfig(fieldDefinition);
                    }

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        refresh();

		return composite;
	}

    public void store() throws Exception {

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        //SourceConfigManager sourceConfigManager = partitionConfig.getSourceConfigManager();
        String oldName = sourceConfig.getName();

        if (!sourceNameText.getText().equals(oldName)) {

            String newName = sourceNameText.getText();

            for (String id : partitionClient.getEntryIds()) {
                EntryClient entryClient = partitionClient.getEntryClient(id);
                EntryConfig entryConfig = entryClient.getEntryConfig();

                EntrySourceConfig s = entryConfig.removeSourceConfig(oldName);
                if (s == null) continue;

                s.setName(newName);
                entryConfig.addSourceConfig(s);

                partitionClient.updateEntry(id, entryConfig);
            }

            //sourceConfigManager.removeSourceConfig(oldName);
            sourceConfig.setName(newName);
            //sourceConfigManager.addSourceConfig(sourceConfig);

        }

        sourceConfig.setParameter("baseDn", baseDnText.getText());
        sourceConfig.setParameter("filter", filterText.getText());
        sourceConfig.setParameter("scope", scopeCombo.getText());
        sourceConfig.setParameter("objectClasses", objectClassesText.getText());

        TableItem[] items = fieldTable.getItems();
        sourceConfig.getFieldConfigs().clear();

        for (TableItem item : items) {
            FieldConfig field = (FieldConfig) item.getData();
            field.setName("".equals(item.getText(1)) ? item.getText(0) : item.getText(1));
            field.setOriginalName(item.getText(0));
            field.setType(item.getText(2));
            field.setPrimaryKey(item.getChecked());
            sourceConfig.addFieldConfig(field);
        }

        partitionClient.updateSource(oldName, sourceConfig);
        partitionClient.store();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();

        checkDirty();
    }

    public void refresh() {
        fieldTable.removeAll();

        Collection<FieldConfig> fields = sourceConfig.getFieldConfigs();
        for (FieldConfig fieldConfig : fields) {

            TableItem item = new TableItem(fieldTable, SWT.CHECK);
            item.setChecked(fieldConfig.isPrimaryKey());
            item.setImage(PenroseStudioPlugin.getImage(fieldConfig.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
            item.setText(0, fieldConfig.getName().equals(fieldConfig.getOriginalName()) ? fieldConfig.getName() : fieldConfig.getOriginalName());
            item.setText(1, fieldConfig.getName().equals(fieldConfig.getOriginalName()) ? "" : fieldConfig.getName());
            item.setText(2, fieldConfig.getType());
            item.setData(fieldConfig);
        }
    }
}
