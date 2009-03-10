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
package org.safehaus.penrose.studio.jdbc.source.editor;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.jdbc.source.wizard.JDBCSourceFieldsWizard;
import org.safehaus.penrose.studio.source.editor.SourceFieldsPage;

public class JDBCSourceFieldsPage extends SourceFieldsPage {
/*
	Table fieldTable;

    Button addButton;
    Button editButton;
    Button removeButton;
*/
    public JDBCSourceFieldsPage(JDBCSourceEditor editor) {
        super(editor);
    }
/*
    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section fieldsSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        fieldsSection.setText("Fields");
        fieldsSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control fieldsControl = createFieldsControl(fieldsSection);
        fieldsSection.setClient(fieldsControl);

        refresh();
    }

    public Composite createFieldsControl(final Composite parent) {

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

                    PenroseClient client = server.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
                    ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(sourceConfig.getConnectionName());
                    ConnectionConfig connectionConfig = connectionClient.getMappingConfig();

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
                    ErrorDialog.open(e);
                }
            }

            public void mouseUp(MouseEvent e) {
                for (int i=0; i<fieldTable.getItemCount(); i++) {
                    TableItem item = fieldTable.getItem(i);
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    fieldDefinition.setPrimaryKey(item.getChecked());
                    item.setImage(PenroseStudio.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
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
                    item.setImage(PenroseStudio.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
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

        addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");

        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    FieldConfig fieldDefinition = new FieldConfig();

                    PenroseClient client = server.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
                    ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(sourceConfig.getConnectionName());
                    ConnectionConfig connectionConfig = connectionClient.getMappingConfig();

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
                    ErrorDialog.open(e);
                }
            }
        });

        editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("Edit");

        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (fieldTable.getSelectionCount() == 0) return;

                    int index = fieldTable.getSelectionIndex();
                    TableItem item = fieldTable.getSelection()[0];
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    String oldName = fieldDefinition.getName();

                    PenroseClient client = server.getClient();
                    PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
                    PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
                    ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
                    ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(sourceConfig.getConnectionName());
                    ConnectionConfig connectionConfig = connectionClient.getMappingConfig();

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
                    ErrorDialog.open(e);
                }
            }
        });

        removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
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
                    ErrorDialog.open(e);
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
            item.setImage(PenroseStudio.getImage(fieldDefinition.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
            item.setText(0, fieldDefinition.getName().equals(fieldDefinition.getOriginalName()) ? fieldDefinition.getName() : fieldDefinition.getOriginalName());
            item.setText(1, fieldDefinition.getName().equals(fieldDefinition.getOriginalName()) ? "" : fieldDefinition.getName());
            item.setText(2, fieldDefinition.getType());
            item.setData(fieldDefinition);
        }
    }

    public void checkDirty() {
        editor.checkDirty();
    }
*/
    public void edit() throws Exception {

        JDBCSourceFieldsWizard wizard = new JDBCSourceFieldsWizard();
        wizard.setServer(server);
        wizard.setPartitionName(partitionName);
        wizard.setSourceConfig(sourceConfig);

        WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
        dialog.setPageSize(600, 300);
        int rc = dialog.open();

        if (rc == Window.CANCEL) return;
    
        refresh();
    }
}