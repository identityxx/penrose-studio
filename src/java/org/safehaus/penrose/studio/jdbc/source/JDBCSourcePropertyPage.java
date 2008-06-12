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
package org.safehaus.penrose.studio.jdbc.source;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.source.JDBCSource;
import org.safehaus.penrose.management.connection.ConnectionClient;
import org.safehaus.penrose.management.partition.PartitionClient;
import org.safehaus.penrose.management.partition.PartitionManagerClient;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.source.FieldDialog;
import org.safehaus.penrose.studio.source.editor.SourceEditorPage;

import java.util.Collection;
import java.util.Iterator;

public class JDBCSourcePropertyPage extends SourceEditorPage {

    Text sourceNameText;
	Combo connectionNameCombo;

    Text catalogText;
    Text schemaText;
	Text tableText;
    Text filterText;
	Table fieldTable;

    Button addButton;
    Button editButton;
    Button removeButton;

    public JDBCSourcePropertyPage(JDBCSourceEditor editor) {
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
        section.setText("Database Info");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control tableSection = createDatabaseSection(section);
        section.setClient(tableSection);

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
	
	public Composite createDatabaseSection(Composite parent) {

		Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(2, false));

        Label catalogLabel = toolkit.createLabel(composite, "Catalog:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        catalogLabel.setLayoutData(gd);

        catalogText = toolkit.createText(composite, sourceConfig.getParameter(JDBCSource.CATALOG), SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        catalogText.setLayoutData(gd);

        catalogText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(catalogText.getText())) {
                    sourceConfig.removeParameter(JDBCSource.CATALOG);
                } else {
                    sourceConfig.setParameter(JDBCSource.CATALOG, catalogText.getText());
                }
                checkDirty();
            }
        });

        Label schemaLabel = toolkit.createLabel(composite, "Schema:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        schemaLabel.setLayoutData(gd);

        schemaText = toolkit.createText(composite, sourceConfig.getParameter(JDBCSource.SCHEMA), SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        schemaText.setLayoutData(gd);

        schemaText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(schemaText.getText())) {
                    sourceConfig.removeParameter(JDBCSource.SCHEMA);
                } else {
                    sourceConfig.setParameter(JDBCSource.SCHEMA, schemaText.getText());
                }
                checkDirty();
            }
        });

		Label tableLabel = toolkit.createLabel(composite, "Table:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        tableLabel.setLayoutData(gd);

        String tableName = sourceConfig.getParameter(JDBCSource.TABLE);

		tableText = toolkit.createText(composite, tableName, SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        tableText.setLayoutData(gd);

        tableText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(tableText.getText())) {
                    sourceConfig.removeParameter(JDBCSource.TABLE);
                } else {
                    sourceConfig.setParameter(JDBCSource.TABLE, tableText.getText());
                }
                checkDirty();
            }
        });

        Label filterLabel = toolkit.createLabel(composite, "Filter:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        filterLabel.setLayoutData(gd);

        filterText = toolkit.createText(composite, sourceConfig.getParameter("filter"), SWT.BORDER);
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
                try {
                    if (fieldTable.getSelectionCount() == 0) return;

                    int index = fieldTable.getSelectionIndex();
                    TableItem item = fieldTable.getSelection()[0];
                    FieldConfig fieldConfig = (FieldConfig)item.getData();
                    String oldName = fieldConfig.getName();

                    PenroseClient client = project.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    ConnectionClient connectionClient = partitionClient.getConnectionClient(sourceConfig.getConnectionName());
                    ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();

                    //PartitionConfigManager partitionConfigManager = project.getPartitionConfigManager();
                    //PartitionConfig partitionConfig = partitionConfigManager.getPartitionConfig(sourceConfig);
                    //ConnectionConfig connectionConfig = partitionConfig.getConnectionConfigManager().getConnectionConfig(sourceConfig.getConnectionName());

                    JDBCClient jdbcClient = new JDBCClient(connectionConfig.getParameters());
                    
                    String catalog = sourceConfig.getParameter(JDBCSource.CATALOG);
                    String schema = sourceConfig.getParameter(JDBCSource.SCHEMA);
                    String table = sourceConfig.getParameter(JDBCSource.TABLE);

                    Collection<FieldConfig> fields = jdbcClient.getColumns(catalog, schema, table);
                    jdbcClient.close();

                    JDBCFieldDialog dialog = new JDBCFieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setColumns(fields);
                    dialog.setFieldConfig(fieldConfig);
                    dialog.open();

                    if (dialog.getAction() == FieldDialog.CANCEL) return;

                    String newName = fieldConfig.getName();

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
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    ConnectionClient connectionClient = partitionClient.getConnectionClient(sourceConfig.getConnectionName());
                    ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();

                    //PartitionConfigManager partitionConfigManager = project.getPartitionConfigManager();
                    //PartitionConfig partitionConfig = partitionConfigManager.getPartitionConfig(sourceConfig);
                    //ConnectionConfig connectionConfig = partitionConfig.getConnectionConfigManager().getConnectionConfig(sourceConfig.getConnectionName());

                    JDBCClient jdbcClient = new JDBCClient(connectionConfig.getParameters());

                    String catalog = sourceConfig.getParameter(JDBCSource.CATALOG);
                    String schema = sourceConfig.getParameter(JDBCSource.SCHEMA);
                    String table = sourceConfig.getParameter(JDBCSource.TABLE);

                    Collection<FieldConfig> fields = jdbcClient.getColumns(catalog, schema, table);
                    jdbcClient.close();

                    JDBCFieldDialog dialog = new JDBCFieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setColumns(fields);
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
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    ConnectionClient connectionClient = partitionClient.getConnectionClient(sourceConfig.getConnectionName());
                    ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();

                    //PartitionConfigManager partitionConfigManager = project.getPartitionConfigManager();
                    //PartitionConfig partitionConfig = partitionConfigManager.getPartitionConfig(sourceConfig);
                    //ConnectionConfig connectionConfig = partitionConfig.getConnectionConfigManager().getConnectionConfig(sourceConfig.getConnectionName());

                    JDBCClient jdbcClient = new JDBCClient(connectionConfig.getParameters());

                    String catalog = sourceConfig.getParameter(JDBCSource.CATALOG);
                    String schema = sourceConfig.getParameter(JDBCSource.SCHEMA);
                    String table = sourceConfig.getParameter(JDBCSource.TABLE);

                    Collection<FieldConfig> fields = jdbcClient.getColumns(catalog, schema, table);
                    jdbcClient.close();

                    JDBCFieldDialog dialog = new JDBCFieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setColumns(fields);
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
                    for (int i=0; i<items.length; i++) {
                        FieldConfig fieldDefinition = (FieldConfig)items[i].getData();
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

    public void refresh() {
        fieldTable.removeAll();

        Collection fields = sourceConfig.getFieldConfigs();
        for (Iterator i=fields.iterator(); i.hasNext(); ) {
            FieldConfig fieldDefinition = (FieldConfig)i.next();

            TableItem item = new TableItem(fieldTable, SWT.CHECK);
            item.setChecked(fieldDefinition.isPrimaryKey());
            item.setImage(PenroseStudioPlugin.getImage(fieldDefinition.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
            item.setText(0, fieldDefinition.getName().equals(fieldDefinition.getOriginalName()) ? fieldDefinition.getName() : fieldDefinition.getOriginalName());
            item.setText(1, fieldDefinition.getName().equals(fieldDefinition.getOriginalName()) ? "" : fieldDefinition.getName());
            item.setText(2, fieldDefinition.getType());
            item.setData(fieldDefinition);
        }
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}
