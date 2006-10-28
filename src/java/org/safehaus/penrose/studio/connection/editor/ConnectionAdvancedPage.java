package org.safehaus.penrose.studio.connection.editor;

import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.safehaus.penrose.studio.parameter.ParameterDialog;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class ConnectionAdvancedPage extends ConnectionEditorPage {

    Text nameText;
    Text adapterText;
    Text descriptionText;

    Table parametersTable;

    Text driverText;
    Text urlText;
    Text usernameText;
    Text passwordText;

    public ConnectionAdvancedPage(ConnectionEditor editor) {
        super(editor, "ADVANCED", "  Advanced  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section propertiesSection = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        propertiesSection.setText("Properties");
        propertiesSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control propertiesControl = createPropertiesControl(propertiesSection);
        propertiesSection.setClient(propertiesControl);

        Section parametersSection = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        parametersSection.setText("Parameters");
        parametersSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control parametersControl = createParametersControl(parametersSection);
        parametersSection.setClient(parametersControl);
    }

    public void refresh() {
        nameText.setText(getConnectionConfig().getName() == null ? "" : getConnectionConfig().getName());
        adapterText.setText(getConnectionConfig().getAdapterName() == null ? "" : getConnectionConfig().getAdapterName());
        descriptionText.setText(getConnectionConfig().getDescription() == null ? "" : getConnectionConfig().getDescription());

        parametersTable.removeAll();

        for (Iterator i=getConnectionConfig().getParameterNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            String value = getConnectionConfig().getParameter(name);

            TableItem ti = new TableItem(parametersTable, SWT.NONE);
            ti.setText(0, name);
            ti.setText(1, value);
        }
    }

    public Composite createPropertiesControl(final Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label nameLabel = getToolkit().createLabel(composite, "Name:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        nameLabel.setLayoutData(gd);

        nameText = getToolkit().createText(composite, "", SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getConnectionConfig().setName("".equals(nameText.getText()) ? null : nameText.getText());
                checkDirty();
            }
        });

        Label adapterNameLabel = getToolkit().createLabel(composite, "Adapter:");
        gd = new GridData();
        gd.widthHint = 100;
        adapterNameLabel.setLayoutData(gd);

        adapterText = getToolkit().createText(composite, "", SWT.BORDER);
        adapterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        adapterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getConnectionConfig().setAdapterName("".equals(adapterText.getText()) ? null : adapterText.getText());
                checkDirty();
            }
        });

        Label descriptionNameLabel = getToolkit().createLabel(composite, "Description:");
        gd = new GridData();
        gd.widthHint = 100;
        descriptionNameLabel.setLayoutData(gd);

        descriptionText = getToolkit().createText(composite, "", SWT.BORDER);
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getConnectionConfig().setDescription("".equals(descriptionText.getText()) ? null : descriptionText.getText());
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

        TableColumn tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(200);

        tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(400);

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
                        getConnectionConfig().removeParameter(oldName);
                    }

                    getConnectionConfig().setParameter(newName, dialog.getValue());

                    refresh();
                    parametersTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

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

                    getConnectionConfig().setParameter(dialog.getName(), dialog.getValue());

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
                        getConnectionConfig().removeParameter(oldName);
                    }

                    getConnectionConfig().setParameter(newName, dialog.getValue());

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
                        getConnectionConfig().removeParameter(name);
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
