package org.safehaus.penrose.studio.nis.source;

import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.studio.jndi.source.JNDIFieldDialog;
import org.safehaus.penrose.studio.source.FieldDialog;
import org.safehaus.penrose.studio.source.editor.SourceEditorPage;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.source.SourceConfigs;
import org.safehaus.penrose.directory.EntryMapping;
import org.safehaus.penrose.directory.SourceMapping;
import org.safehaus.penrose.nis.*;
import org.safehaus.penrose.nis.connection.NISConnection;

import java.util.Collection;
import java.util.ArrayList;

public class NISSourcePropertyPage extends SourceEditorPage {

	Text sourceNameText;
    Combo connectionNameCombo;

	Text baseText;
    Text objectClassesText;

	Table fieldTable;

    Button addButton;
    Button editButton;
    Button removeButton;

	NISClient client;

	String[] scopes = new String[] { "OBJECT", "ONELEVEL", "SUBTREE" };

    public NISSourcePropertyPage(NISSourceEditor editor) throws Exception {
        super(editor, "PROPERTIES", "  Properties  ");

        ConnectionConfig connectionConfig = partitionConfig.getConnectionConfigs().getConnectionConfig(sourceConfig.getConnectionName());
        if (connectionConfig == null) return;

        String method = connectionConfig.getParameter(NISConnection.METHOD);
        if (method == null) method = NISConnection.DEFAULT_METHOD;

        if (NISConnection.LOCAL.equals(method)) {
            client = new NISLocalClient();

        } else if (NISConnection.YP.equals(method)) {
            client = new NISYPClient();

        } else { // if (METHOD_JNDI.equals(method)) {
            client = new NISJNDIClient();
        }

        client.init(connectionConfig.getParameters());
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

        for (ConnectionConfig connectionConfig : partitionConfig.getConnectionConfigs().getConnectionConfigs()) {
            connectionNameCombo.add(connectionConfig.getName());
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
					item.setImage(PenroseStudioPlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
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
					item.setImage(PenroseStudioPlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
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

        addButton = toolkit.createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    FieldConfig fieldDefinition = new FieldConfig();

                    SchemaManager schemaManager = project.getSchemaManager();
                    Collection<AttributeType> attributeTypes = schemaManager.getAttributeTypes();

                    JNDIFieldDialog dialog = new JNDIFieldDialog(parent.getShell(), SWT.NONE);
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

                    SchemaManager schemaManager = project.getSchemaManager();
                    Collection<AttributeType> attributeTypes = schemaManager.getAttributeTypes();

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
                    setDirty(true);

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

        SourceConfigs sources = partitionConfig.getSourceConfigs();

        if (!sourceNameText.getText().equals(sourceConfig.getName())) {

            String oldName = sourceConfig.getName();
            String newName = sourceNameText.getText();

            Collection<EntryMapping> entries = partitionConfig.getDirectoryConfig().getEntryMappings();
            for (EntryMapping entry : entries) {

                SourceMapping s = entry.removeSourceMapping(oldName);
                if (s == null) continue;

                s.setName(newName);
                entry.addSourceMapping(s);
            }

            sources.removeSourceConfig(oldName);
            sourceConfig.setName(newName);
            sources.addSourceConfig(sourceConfig);
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
            item.setImage(PenroseStudioPlugin.getImage(fieldConfig.isPrimaryKey() ? PenroseImage.KEY : PenroseImage.NOKEY));
            item.setText(0, fieldConfig.getName().equals(fieldConfig.getOriginalName()) ? fieldConfig.getName() : fieldConfig.getOriginalName());
            item.setText(1, fieldConfig.getName().equals(fieldConfig.getOriginalName()) ? "" : fieldConfig.getName());
            item.setText(2, fieldConfig.getType());
            item.setData(fieldConfig);
        }
    }
}
