package org.safehaus.penrose.studio.source.editor;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.parameter.ParameterDialog;
import org.safehaus.penrose.connection.ConnectionConfig;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map;

public class SourceAdvancedPage extends SourceEditorPage {

    Text sourceNameText;
    Combo connectionNameCombo;
    Text descriptionText;

    Table parametersTable;
    Table fieldsTable;

    public SourceAdvancedPage(SourceEditor editor) {
        super(editor, "ADVANCED", "  Advanced  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Properties");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control propertiesControl = createPropertiesControl(section);
        section.setClient(propertiesControl);

        section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Parameters");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control createParametersControl = createParametersControl(section);
        section.setClient(createParametersControl);

        section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Fields");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control fieldsControl = createFieldsControl(section);
        section.setClient(fieldsControl);
    }

    public void refresh() {

        String sourceName = getSourceConfig().getName();
        sourceNameText.setText(sourceName == null ? "" : sourceName);

        String connectionName = getSourceConfig().getConnectionName();
        connectionNameCombo.setText(connectionName == null ? "" : connectionName);

        String description = getSourceConfig().getDescription();
        descriptionText.setText(description == null ? "" : description);

        parametersTable.removeAll();

        for (Iterator i=getSourceConfig().getParameterNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            String value = getSourceConfig().getParameter(name);

            TableItem item = new TableItem(parametersTable, SWT.CHECK);
            item.setText(0, name);
            item.setText(1, value == null ? "" : value);
        }

        fieldsTable.removeAll();

        Collection fields = getSourceConfig().getFieldConfigs();
        Map map = new TreeMap();
        for (Iterator i=fields.iterator(); i.hasNext(); ) {
            FieldConfig fieldDefinition = (FieldConfig)i.next();
            map.put(fieldDefinition.getOriginalName(), fieldDefinition);
        }

        for (Iterator i=map.values().iterator(); i.hasNext(); ) {
            FieldConfig fieldConfig = (FieldConfig)i.next();

            TableItem item = new TableItem(fieldsTable, SWT.CHECK);
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

        getToolkit().createLabel(composite, "Connection Name:");

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

    public Composite createParametersControl(Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        parametersTable = getToolkit().createTable(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        parametersTable.setHeaderVisible(true);
        parametersTable.setLinesVisible(true);

        parametersTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        parametersTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    if (parametersTable.getSelectionCount() == 0) return;

                    int index = parametersTable.getSelectionIndex();
                    TableItem item = parametersTable.getSelection()[0];
                    String oldName = item.getText(0);

                    ParameterDialog dialog = new ParameterDialog(getEditor().getSite().getShell(), SWT.NONE);
                    dialog.setText("Edit parameter...");
                    dialog.setName(oldName);
                    dialog.setValue(item.getText(1));
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    String newName = dialog.getName();

                    if (!oldName.equals(newName)) {
                        getSourceConfig().removeParameter(oldName);
                    }

                    getSourceConfig().setParameter(newName, dialog.getValue());

                    refresh();
                    parametersTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        TableColumn tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(200);

        tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(400);

        Composite buttons = getToolkit().createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = getToolkit().createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ParameterDialog dialog = new ParameterDialog(getEditor().getSite().getShell(), SWT.NONE);
                    dialog.setText("Add parameter...");
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    getSourceConfig().setParameter(dialog.getName(), dialog.getValue());

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Button editButton = getToolkit().createButton(buttons, "Edit", SWT.PUSH);
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parametersTable.getSelectionCount() == 0) return;

                    int index = parametersTable.getSelectionIndex();
                    TableItem item = parametersTable.getSelection()[0];
                    String oldName = item.getText(0);

                    ParameterDialog dialog = new ParameterDialog(getEditor().getSite().getShell(), SWT.NONE);
                    dialog.setText("Edit parameter...");
                    dialog.setName(oldName);
                    dialog.setValue(item.getText(1));
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    String newName = dialog.getName();

                    if (!oldName.equals(newName)) {
                        getSourceConfig().removeParameter(oldName);
                    }

                    getSourceConfig().setParameter(newName, dialog.getValue());

                    refresh();
                    parametersTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Button removeButton = getToolkit().createButton(buttons, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parametersTable.getSelectionCount() == 0) return;

                    TableItem items[] = parametersTable.getSelection();
                    for (int i=0; i<items.length; i++) {
                        String name = items[i].getText(0);
                        getSourceConfig().removeParameter(name);
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

    public Composite createFieldsControl(final Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        fieldsTable = getToolkit().createTable(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK | SWT.MULTI);
        fieldsTable.setHeaderVisible(true);
        fieldsTable.setLinesVisible(true);

        fieldsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        fieldsTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    if (fieldsTable.getSelectionCount() == 0) return;

                    int index = fieldsTable.getSelectionIndex();
                    TableItem item = fieldsTable.getSelection()[0];
                    FieldConfig fieldConfig = (FieldConfig)item.getData();
                    String oldName = fieldConfig.getName();

                    FieldDialog dialog = new FieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setFieldConfig(fieldConfig);
                    dialog.open();

                    if (dialog.getAction() == FieldDialog.CANCEL) return;

                    String newName = fieldConfig.getName();

                    if (!oldName.equals(newName)) {
                        getSourceConfig().renameFieldConfig(oldName, newName);
                    }

                    refresh();
                    fieldsTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }

            public void mouseUp(MouseEvent e) {
                for (int i=0; i<fieldsTable.getItemCount(); i++) {
                    TableItem item = fieldsTable.getItem(i);
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    fieldDefinition.setPrimaryKey(item.getChecked()+"");
                    item.setImage(PenrosePlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
                }

                checkDirty();
            }
        });

        fieldsTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                for (int i=0; i<fieldsTable.getItemCount(); i++) {
                    TableItem item = fieldsTable.getItem(i);
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    fieldDefinition.setPrimaryKey(item.getChecked()+"");
                    item.setImage(PenrosePlugin.getImage(item.getChecked() ? PenroseImage.KEY : PenroseImage.NOKEY));
                }

                checkDirty();
            }
        });

        TableColumn tc = new TableColumn(fieldsTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(250);

        tc = new TableColumn(fieldsTable, SWT.NONE);
        tc.setText("Alias");
        tc.setWidth(250);

        tc = new TableColumn(fieldsTable, SWT.NONE);
        tc.setText("Type");
        tc.setWidth(100);

        Composite buttons = getToolkit().createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = getToolkit().createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    FieldConfig fieldDefinition = new FieldConfig();

                    FieldDialog dialog = new FieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setFieldConfig(fieldDefinition);
                    dialog.open();

                    if (dialog.getAction() == FieldDialog.CANCEL) return;

                    getSourceConfig().addFieldConfig(fieldDefinition);

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Button editButton = getToolkit().createButton(buttons, "Edit", SWT.PUSH);
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (fieldsTable.getSelectionCount() == 0) return;

                    int index = fieldsTable.getSelectionIndex();
                    TableItem item = fieldsTable.getSelection()[0];
                    FieldConfig fieldDefinition = (FieldConfig)item.getData();
                    String oldName = fieldDefinition.getName();

                    FieldDialog dialog = new FieldDialog(parent.getShell(), SWT.NONE);
                    dialog.setFieldConfig(fieldDefinition);
                    dialog.open();

                    if (dialog.getAction() == FieldDialog.CANCEL) return;

                    String newName = fieldDefinition.getName();

                    if (!oldName.equals(newName)) {
                        getSourceConfig().renameFieldConfig(oldName, newName);
                    }

                    refresh();
                    fieldsTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Button removeButton = getToolkit().createButton(buttons, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (fieldsTable.getSelectionCount() == 0) return;

                    TableItem items[] = fieldsTable.getSelection();
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
