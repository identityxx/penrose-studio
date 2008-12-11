package org.safehaus.penrose.studio.nis.source;

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
import org.safehaus.penrose.directory.EntryClient;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.nis.*;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.ldap.source.LDAPFieldDialog;
import org.safehaus.penrose.studio.source.FieldDialog;
import org.safehaus.penrose.studio.source.editor.SourceEditorPage;
import org.safehaus.penrose.client.PenroseClient;

import java.util.ArrayList;
import java.util.Collection;

public class NISSourcePropertyPage extends SourceEditorPage {

	Text sourceNameText;
    Combo connectionNameCombo;

	Text baseText;
    Text objectClassesText;

	Table fieldTable;

    Button addButton;
    Button editButton;
    Button removeButton;

	NISClient nisClient;

	String[] scopes = new String[] { "OBJECT", "ONELEVEL", "SUBTREE" };

    public NISSourcePropertyPage(NISSourceEditor editor) throws Exception {
        super(editor, "PROPERTIES", "  Properties  ");

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
        
        ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(sourceConfig.getConnectionName());

        ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();
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
                setDirty(true);
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
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

            for (String connectionName : connectionManagerClient.getConnectionNames()) {
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
                setDirty(true);
            }
        });

        return composite;
	}

	public Composite createDirectorySection(Composite parent) {

		Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(2, false));

		Label baseDnLabel = toolkit.createLabel(composite, "Base:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        baseDnLabel.setLayoutData(gd);

        String s = sourceConfig.getParameter("base");
		baseText = toolkit.createText(composite, s == null ? "" : s, SWT.BORDER);
        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        baseText.setLayoutData(gd);

        baseText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(baseText.getText())) {
                    sourceConfig.removeParameter("base");
                } else {
                    sourceConfig.setParameter("base", baseText.getText());
                }
                setDirty(true);
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
                setDirty(true);
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

                    PenroseClient client = project.getClient();
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

                    PenroseClient client = project.getClient();
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

    public void store() throws Exception {

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
        DirectoryClient directoryClient = partitionClient.getDirectoryClient();

        //SourceConfigManager sources = partitionConfig.getSourceConfigManager();
        String oldName = sourceConfig.getName();

        if (!sourceNameText.getText().equals(oldName)) {

            String newName = sourceNameText.getText();

            for (String id : directoryClient.getEntryIds()) {
                EntryClient entryClient = directoryClient.getEntryClient(id);
                EntryConfig entryConfig = entryClient.getEntryConfig();

                EntrySourceConfig s = entryConfig.removeSourceConfig(oldName);
                if (s == null) continue;

                s.setAlias(newName);
                entryConfig.addSourceConfig(s);

                directoryClient.updateEntry(id, entryConfig);
            }

            //sources.removeSourceConfig(oldName);
            sourceConfig.setName(newName);
            //sources.addSourceConfig(sourceConfig);
        }

        sourceConfig.setParameter("base", baseText.getText());
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

        sourceManagerClient.updateSource(oldName, sourceConfig);
        partitionClient.store();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();

        setDirty(false);
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
}
