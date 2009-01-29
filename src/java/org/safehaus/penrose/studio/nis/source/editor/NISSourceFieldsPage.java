package org.safehaus.penrose.studio.nis.source.editor;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.nis.source.wizard.NISSourceFieldsWizard;
import org.safehaus.penrose.studio.source.editor.SourceFieldsPage;

public class NISSourceFieldsPage extends SourceFieldsPage {
/*
	Table fieldTable;

    Button addButton;
    Button editButton;
    Button removeButton;

	NISClient nisClient;

	String[] scopes = new String[] { "OBJECT", "ONELEVEL", "SUBTREE" };
*/
    public NISSourceFieldsPage(NISSourceEditor editor) throws Exception {
        super(editor);
    }
/*
    public NISSourceFieldsPage(NISSourceEditor editor) throws Exception {
        super(editor);

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

        ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(sourceConfig.getConnectionName());

        ConnectionConfig connectionConfig = connectionClient.getMappingConfig();
        if (connectionConfig == null) return;

        String method = connectionConfig.getParameter(NIS.METHOD);
        if (method == null) method = NIS.DEFAULT_METHOD;

        if (NIS.LOCAL.equals(method)) {
            nisClient = new NISLocalClient();

        } else if (NIS.YP.equals(method)) {
            nisClient = new NISYPClient();

        } else { // if (METHOD_JNDI.equals(method)) {
            nisClient = new NISJNDIClient();
        }

        nisClient.init(connectionConfig.getParameters());

    }

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
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    String oldName = fieldDefinition.getName();

                    Collection<AttributeType> attributeTypes = new ArrayList<AttributeType>();

                    LDAPFieldDialog dialog = new LDAPFieldDialog(parent.getShell(), SWT.NONE);
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
                    setDirty(true);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

			public void mouseUp(MouseEvent e) {
				for (int i=0; i<fieldTable.getItemCount(); i++) {
					TableItem item = fieldTable.getItem(i);
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    fieldDefinition.setPrimaryKey(item.getChecked());
					item.setImage(PenroseStudio.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
				}

                setDirty(true);
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

                setDirty(true);
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
                    SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
                    Collection<AttributeType> attributeTypes = schemaManagerClient.getAttributeTypes();

                    LDAPFieldDialog dialog = new LDAPFieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setAttributeTypes(attributeTypes);
                    dialog.setFieldConfig(fieldDefinition);
                    dialog.open();

                    if (dialog.getAction() == FieldDialog.CANCEL) return;

                    sourceConfig.addFieldConfig(fieldDefinition);

                    refresh();
                    setDirty(true);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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
                    SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
                    Collection<AttributeType> attributeTypes = schemaManagerClient.getAttributeTypes();

                    LDAPFieldDialog dialog = new LDAPFieldDialog(parent.getShell(), SWT.NONE);
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
                    setDirty(true);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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
                    for (TableItem item : items) {
                        FieldConfig fieldDefinition = (FieldConfig) item.getData();
                        sourceConfig.removeFieldConfig(fieldDefinition);
                    }

                    refresh();
                    setDirty(true);

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

        Collection<FieldConfig> fields = sourceConfig.getFieldConfigs();
        for (FieldConfig fieldConfig : fields) {

            TableItem item = new TableItem(fieldTable, SWT.CHECK);
            item.setChecked(fieldConfig.isPrimaryKey());
            item.setImage(PenroseStudio.getImage(fieldConfig.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
            item.setText(0, fieldConfig.getName().equals(fieldConfig.getOriginalName()) ? fieldConfig.getName() : fieldConfig.getOriginalName());
            item.setText(1, fieldConfig.getName().equals(fieldConfig.getOriginalName()) ? "" : fieldConfig.getName());
            item.setText(2, fieldConfig.getType());
            item.setData(fieldConfig);
        }
    }
*/
    public void edit() throws Exception {

        NISSourceFieldsWizard wizard = new NISSourceFieldsWizard();
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