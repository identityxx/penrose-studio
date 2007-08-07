package org.safehaus.penrose.studio.handler;

import org.apache.log4j.Logger;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.safehaus.penrose.studio.parameter.ParameterDialog;
import org.safehaus.penrose.handler.HandlerConfig;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class HandlerPropertyPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Text handlerNameText;
	Text handlerClassText;
	Text descriptionText;

    Table parameterTable;

    Button addButton;
    Button editButton;
    Button removeButton;

    HandlerEditor editor;
	HandlerConfig handlerConfig;

    public HandlerPropertyPage(HandlerEditor editor) {
        this.editor = editor;
        this.handlerConfig = editor.handlerConfig;
    }

    public Control createControl() {
        toolkit = new FormToolkit(editor.getParent().getDisplay());

        Form form = toolkit.createForm(editor.getParent());
        form.setText("Handler Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Properties");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control propertiesSection = createPropertiesSection(section);
        section.setClient(propertiesSection);

        section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Parameters");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control parametersSection = createParametersSection(section);
        section.setClient(parametersSection);

        return form;
	}

	public Composite createPropertiesSection(Composite parent) {

		Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(2, false));

		Label connectorNameLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        connectorNameLabel.setLayoutData(gd);

		handlerNameText = toolkit.createText(composite, handlerConfig.getName(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
		handlerNameText.setLayoutData(gd);

        handlerNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                handlerConfig.setName(handlerNameText.getText());
                checkDirty();
            }
        });

		Label connectorClassLabel = toolkit.createLabel(composite, "Class:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        connectorClassLabel.setLayoutData(gd);

        handlerClassText = toolkit.createText(composite, handlerConfig.getHandlerClass(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        handlerClassText.setLayoutData(gd);

        handlerClassText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                handlerConfig.setHandlerClass(handlerClassText.getText());
                checkDirty();
            }
        });

        Label descriptionLabel = toolkit.createLabel(composite, "Description:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        descriptionLabel.setLayoutData(gd);

        descriptionText = toolkit.createText(composite, handlerConfig.getDescription(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        descriptionText.setLayoutData(gd);

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                handlerConfig.setDescription(descriptionText.getText());
                checkDirty();
            }
        });

		return composite;
	}

    public Composite createParametersSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

		parameterTable = toolkit.createTable(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        parameterTable.setHeaderVisible(true);
        parameterTable.setLinesVisible(true);

        parameterTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        parameterTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    if (parameterTable.getSelectionCount() == 0) return;

                    int index = parameterTable.getSelectionIndex();
                    TableItem item = parameterTable.getSelection()[0];

                    String oldName = item.getText(0);
                    String oldValue = item.getText(1);

                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Edit parameter...");
                    dialog.setName(oldName);
                    dialog.setValue(oldValue);
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    String newName = dialog.getName();
                    String newValue = dialog.getValue();

                    if (!oldName.equals(newName)) {
                        handlerConfig.removeParameter(oldName);
                    }

                    handlerConfig.setParameter(newName, newValue);

                    refresh();
                    parameterTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        TableColumn tc = new TableColumn(parameterTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(250);

        tc = new TableColumn(parameterTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(250);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        addButton = toolkit.createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add parameter...");
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    handlerConfig.setParameter(dialog.getName(), dialog.getValue());

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
                    if (parameterTable.getSelectionCount() == 0) return;

                    int index = parameterTable.getSelectionIndex();
                    TableItem item = parameterTable.getSelection()[0];

                    String oldName = item.getText(0);
                    String oldValue = item.getText(1);

                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Edit parameter...");
                    dialog.setName(oldName);
                    dialog.setValue(oldValue);
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    String newName = dialog.getName();
                    String newValue = dialog.getValue();

                    if (!oldName.equals(newName)) {
                        handlerConfig.removeParameter(oldName);
                    }

                    handlerConfig.setParameter(newName, newValue);

                    refresh();
                    parameterTable.setSelection(index);
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
                    if (parameterTable.getSelectionCount() == 0) return;

                    TableItem items[] = parameterTable.getSelection();
                    for (int i=0; i<items.length; i++) {
                        String name = items[i].getText(0);
                        handlerConfig.removeParameter(name);
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
        parameterTable.removeAll();

        for (Iterator i=handlerConfig.getParameterNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            String value = handlerConfig.getParameter(name);

            TableItem item = new TableItem(parameterTable, SWT.CHECK);
            item.setText(0, name);
            item.setText(1, value);
        }
    }

    public void checkDirty() {
        editor.checkDirty();
    }

    public void load() {
        refresh();
    }

    public void dispose() {
        toolkit.dispose();
    }
}
