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
package org.safehaus.penrose.studio.service.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.*;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.studio.parameter.ParameterDialog;
import org.safehaus.penrose.studio.service.editor.ServiceEditor;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class ServicePropertyPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Text dnText;
	Text classText;
    Text descriptionText;
    Button enabledCheckbox;

    Table parametersTable;

    Button addButton;
    Button editButton;
    Button removeButton;

    ServiceEditor editor;
    ServiceConfig serviceConfig;

    public ServicePropertyPage(ServiceEditor editor) {
        this.editor = editor;
        this.serviceConfig = editor.serviceConfig;
    }

    public Control createControl() {
        toolkit = new FormToolkit(editor.getParent().getDisplay());

        Form form = toolkit.createForm(editor.getParent());
        form.setText("Service Editor");

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

		Label dnLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        dnLabel.setLayoutData(gd);

		dnText = toolkit.createText(composite, serviceConfig.getName(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
		dnText.setLayoutData(gd);

        dnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                serviceConfig.setName(dnText.getText());
                checkDirty();
            }
        });

		Label passwordLabel = toolkit.createLabel(composite, "Class:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        passwordLabel.setLayoutData(gd);

        classText = toolkit.createText(composite, serviceConfig.getServiceClass(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        classText.setLayoutData(gd);

        classText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                serviceConfig.setServiceClass(classText.getText());
                checkDirty();
            }
        });

        Label descriptionLabel = toolkit.createLabel(composite, "Description:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        descriptionLabel.setLayoutData(gd);

        descriptionText = toolkit.createText(composite, serviceConfig.getDescription(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        descriptionText.setLayoutData(gd);

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                serviceConfig.setDescription(descriptionText.getText());
                checkDirty();
            }
        });

        Label enabledLabel = toolkit.createLabel(composite, "Enabled:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        enabledLabel.setLayoutData(gd);

        enabledCheckbox = new Button(composite, SWT.CHECK);
        enabledCheckbox.setText("");
        enabledCheckbox.setSelection(serviceConfig.isEnabled());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        enabledCheckbox.setLayoutData(gd);

        enabledCheckbox.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                serviceConfig.setEnabled(enabledCheckbox.getSelection());
                checkDirty();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });
		return composite;
	}
	
    public Composite createParametersSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

		parametersTable = toolkit.createTable(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
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
                        serviceConfig.removeParameter(oldName);
                    }

                    serviceConfig.setParameter(newName, newValue);

                    refresh();
                    parametersTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        TableColumn tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(250);

        tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(250);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add parameter...");
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    serviceConfig.setParameter(dialog.getName(), dialog.getValue());

                    refresh();
                    checkDirty();

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
                    if (parametersTable.getSelectionCount() == 0) return;

                    int index = parametersTable.getSelectionIndex();
                    TableItem item = parametersTable.getSelection()[0];

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
                        serviceConfig.removeParameter(oldName);
                    }

                    serviceConfig.setParameter(newName, newValue);

                    refresh();
                    parametersTable.setSelection(index);
                    checkDirty();

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
                    if (parametersTable.getSelectionCount() == 0) return;

                    TableItem items[] = parametersTable.getSelection();
                    for (int i=0; i<items.length; i++) {
                        String name = items[i].getText(0);
                        serviceConfig.removeParameter(name);
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
        parametersTable.removeAll();

        for (Iterator i=serviceConfig.getParameterNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            String value = serviceConfig.getParameter(name);

            TableItem item = new TableItem(parametersTable, SWT.CHECK);
            item.setText(0, name);
            item.setText(1, value);
        }
    }

    public void load() {
        refresh();
    }

    public void checkDirty() {
        editor.checkDirty();
    }

    public void dispose() {
        toolkit.dispose();
    }
}
