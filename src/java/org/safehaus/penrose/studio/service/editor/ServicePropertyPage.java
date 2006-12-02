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
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.studio.parameter.ParameterDialog;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class ServicePropertyPage extends ServiceEditorPage {

    Text dnText;
	Text classText;
    Text descriptionText;
    Button enabledCheckbox;

    Table parametersTable;

    Button addButton;
    Button editButton;
    Button removeButton;

    public ServicePropertyPage(ServiceEditor editor) {
        super(editor, "STATUS", "  Status  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Control propertiesSection = createPropertiesSection(body);
        propertiesSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control parametersSection = createParametersSection(body);
        parametersSection.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

    public void refresh() {
        parametersTable.removeAll();

        for (Iterator i=getServiceConfig().getParameterNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            String value = getServiceConfig().getParameter(name);

            TableItem item = new TableItem(parametersTable, SWT.CHECK);
            item.setText(0, name);
            item.setText(1, value);
        }
    }

	public Section createPropertiesSection(Composite parent) {

        Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Properties");

		Composite composite = toolkit.createComposite(section);
        section.setClient(composite);
		composite.setLayout(new GridLayout(2, false));

		Label dnLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        dnLabel.setLayoutData(gd);

		dnText = toolkit.createText(composite, getServiceConfig().getName(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
		dnText.setLayoutData(gd);

        dnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getServiceConfig().setName(dnText.getText());
                checkDirty();
            }
        });

		Label passwordLabel = toolkit.createLabel(composite, "Class:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        passwordLabel.setLayoutData(gd);

        classText = toolkit.createText(composite, getServiceConfig().getServiceClass(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        classText.setLayoutData(gd);

        classText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getServiceConfig().setServiceClass(classText.getText());
                checkDirty();
            }
        });

        Label descriptionLabel = toolkit.createLabel(composite, "Description:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        descriptionLabel.setLayoutData(gd);

        descriptionText = toolkit.createText(composite, getServiceConfig().getDescription(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        descriptionText.setLayoutData(gd);

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getServiceConfig().setDescription("".equals(descriptionText.getText()) ? null : descriptionText.getText());
                checkDirty();
            }
        });

        Label enabledLabel = toolkit.createLabel(composite, "Enabled:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        enabledLabel.setLayoutData(gd);

        enabledCheckbox = toolkit.createButton(composite, "", SWT.CHECK);
        enabledCheckbox.setSelection(getServiceConfig().isEnabled());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        enabledCheckbox.setLayoutData(gd);

        enabledCheckbox.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                getServiceConfig().setEnabled(enabledCheckbox.getSelection());
                checkDirty();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        return section;
	}
	
    public Composite createParametersSection(final Composite parent) {

        Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Parameters");

        Composite composite = toolkit.createComposite(section);
        section.setClient(composite);
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
                        getServiceConfig().removeParameter(oldName);
                    }

                    getServiceConfig().setParameter(newName, newValue);

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
        tc.setWidth(250);

        tc = new TableColumn(parametersTable, SWT.NONE);
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

                    getServiceConfig().setParameter(dialog.getName(), dialog.getValue());

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        editButton = toolkit.createButton(buttons, "Edit", SWT.PUSH);
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
                        getServiceConfig().removeParameter(oldName);
                    }

                    getServiceConfig().setParameter(newName, newValue);

                    refresh();
                    parametersTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        removeButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parametersTable.getSelectionCount() == 0) return;

                    TableItem items[] = parametersTable.getSelection();
                    for (int i=0; i<items.length; i++) {
                        String name = items[i].getText(0);
                        getServiceConfig().removeParameter(name);
                    }

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

		return section;
	}
}
