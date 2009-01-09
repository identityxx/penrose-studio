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
package org.safehaus.penrose.studio.module.editor;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.studio.parameter.ParameterDialog;

import java.util.Collection;

public class ModuleSettingsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table parametersTable;
    Table mappingsTable;

    boolean dirty;

    ModuleEditor editor;
    ModuleConfig moduleConfig;
    Collection<ModuleMapping> moduleMappings;

    public ModuleSettingsPage(ModuleEditor editor) {
        super(editor, "SETTINGS", "  Settings  ");

        this.editor = editor;
        this.moduleConfig = editor.moduleConfig;
        this.moduleMappings = editor.moduleMappings;
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Module Settings");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section propertiesSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        propertiesSection.setText("Module Editor");
        propertiesSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite propertiesComponent = createPropertiesComponent(propertiesSection);
        propertiesSection.setClient(propertiesComponent);

        Section parametersSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        parametersSection.setText("Parameters");
        parametersSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite parametersComponent = createParametersComponent(parametersSection);
        parametersSection.setClient(parametersComponent);

        Section mappingsSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        mappingsSection.setText("Mappings");
        mappingsSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite mappingsComponent = createMappingsComponent(mappingsSection);
        mappingsSection.setClient(mappingsComponent);

        refresh();
	}

	public Composite createPropertiesComponent(Composite parent) {

		Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

		Label nameLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        nameLabel.setLayoutData(gd);

		final Text nameText = toolkit.createText(composite, "", SWT.BORDER);
        if (moduleConfig.getName() != null) nameText.setText(moduleConfig.getName());
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                moduleConfig.setName(nameText.getText());
                checkDirty();
            }
        });

		toolkit.createLabel(composite, "Class:");

		final Text classText = toolkit.createText(composite, "", SWT.BORDER);
        if (moduleConfig.getModuleClass() != null) classText.setText(moduleConfig.getModuleClass());
        classText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        classText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                moduleConfig.setModuleClass(classText.getText());
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Description:");

        final Text descriptionText = toolkit.createText(composite, "", SWT.BORDER);
        if (moduleConfig.getDescription() != null) descriptionText.setText(moduleConfig.getDescription());
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String s = descriptionText.getText();
                moduleConfig.setDescription("".equals(s) ? null : s);
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Enabled:");

        final Button enabledCheckbox = new Button(composite, SWT.CHECK);
		enabledCheckbox.setText("");

        enabledCheckbox.setSelection(moduleConfig.isEnabled());
        enabledCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        enabledCheckbox.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                moduleConfig.setEnabled(enabledCheckbox.getSelection());
                checkDirty();
            }
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        return composite;
    }

    public Composite createParametersComponent(final Composite parent) {

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
                        moduleConfig.removeParameter(oldName);
                    }

                    moduleConfig.setParameter(newName, newValue);

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
        tc.setWidth(300);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
		addButton.setText("Add");

        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add parameter...");
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    moduleConfig.setParameter(dialog.getName(), dialog.getValue());

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
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
                        moduleConfig.removeParameter(oldName);
                    }

                    moduleConfig.setParameter(newName, newValue);

                    refresh();
                    parametersTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText("Remove");

        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parametersTable.getSelectionCount() == 0) return;

                    TableItem items[] = parametersTable.getSelection();
                    for (TableItem item : items) {
                        String name = item.getText(0);
                        moduleConfig.removeParameter(name);
                    }

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

		return composite;
	}

    public void refresh() {
        parametersTable.removeAll();

        for (String name : moduleConfig.getParameterNames()) {
            String value = moduleConfig.getParameter(name);

            TableItem item = new TableItem(parametersTable, SWT.NONE);
            item.setText(0, name);
            item.setText(1, value);
        }

        if (moduleMappings != null) {
            for (ModuleMapping mapping : moduleMappings) {
                TableItem tableItem = new TableItem(mappingsTable, SWT.NONE);
                tableItem.setText(0, mapping.getBaseDn().toString());
                tableItem.setText(1, mapping.getScope() == null ? "" : mapping.getScope());
                tableItem.setText(2, mapping.getFilter() == null ? "" : mapping.getFilter());
                tableItem.setData(mapping);
            }
        }
    }

	public Composite createMappingsComponent(final Composite parent) {

		Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(2, false));

        mappingsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        mappingsTable.setHeaderVisible(true);
        mappingsTable.setLinesVisible(true);
        mappingsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(mappingsTable, SWT.LEFT, 0);
        tc.setText("Base DN");
        tc.setWidth(250);

        tc = new TableColumn(mappingsTable, SWT.LEFT, 1);
        tc.setText("Scope");
        tc.setWidth(100);

        tc = new TableColumn(mappingsTable, SWT.LEFT, 2);
        tc.setText("Filter");
        tc.setWidth(200);

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
		addButton.setText("Add");

        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                ModuleMapping mapping = new ModuleMapping();
                mapping.setScope("SUBTREE");
                mapping.setFilter("(objectClass=*)");

                ModuleMappingDialog dialog = new ModuleMappingDialog(parent.getShell(), SWT.NONE);
                dialog.setMapping(mapping);
                dialog.open();

                if (dialog.getAction() == ModuleMappingDialog.CANCEL) return;

                TableItem item = new TableItem(mappingsTable, SWT.NONE);
                item.setText(0, mapping.getBaseDn().toString());
                item.setText(1, mapping.getScope());
                item.setText(2, mapping.getFilter());
                item.setData(mapping);
                checkDirty();
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText("Remove");

        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (mappingsTable.getSelectionCount() == 0) return;

                TableItem item = mappingsTable.getSelection()[0];
                item.dispose();
                checkDirty();
            }
        });

		return composite;
	}

    public void checkDirty() {
        editor.checkDirty();
    }
}