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
 */package org.safehaus.penrose.studio.mapping.editor;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.mapping.Expression;
import org.safehaus.penrose.mapping.MappingFieldConfig;
import org.safehaus.penrose.mapping.MappingConfig;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.mapping.wizard.AddFieldMappingWizard;
import org.safehaus.penrose.studio.mapping.wizard.EditFieldMappingWizard;

/**
 * @author Endi S. Dewata
 */
public class MappingEditorFieldsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table fieldMappings;

    MappingEditor editor;
    MappingConfig mappingConfig;

    public MappingEditorFieldsPage(MappingEditor editor) {
        super(editor, "FIELDS", "  Fields  ");

        this.editor = editor;
        this.mappingConfig = editor.mappingConfig;
    }

    public void createFormContent(IManagedForm managedForm) {
        try {
            toolkit = managedForm.getToolkit();

            ScrolledForm form = managedForm.getForm();
            form.setText("Mapping Editor");

            Composite body = form.getBody();
            body.setLayout(new GridLayout());

            Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            section.setText("Fields");
            section.setLayoutData(new GridData(GridData.FILL_BOTH));

            Control atSection = createFieldSection(section);
            section.setClient(atSection);

            refresh();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Composite createFieldSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        fieldMappings = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        fieldMappings.setHeaderVisible(true);
        fieldMappings.setLinesVisible(true);

        fieldMappings.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(this.fieldMappings, SWT.LEFT);
        tc.setText("Name");
        tc.setWidth(140);

        tc = new TableColumn(fieldMappings, SWT.LEFT);
        tc.setText("Value");
        tc.setWidth(200);

        tc = new TableColumn(fieldMappings, SWT.LEFT);
        tc.setText("Required");
        tc.setWidth(75);

        tc = new TableColumn(fieldMappings, SWT.LEFT);
        tc.setText("Condition");
        tc.setWidth(150);

        fieldMappings.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    editField();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Menu menu = new Menu(fieldMappings);
        fieldMappings.setMenu(menu);

        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText("Edit");

        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    editField();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = toolkit.createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    addField();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button editButton = toolkit.createButton(buttons, "Edit", SWT.PUSH);
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    editField();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button removeButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    removeField();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        new Label(buttons, SWT.NONE);

        Button moveUpButton = toolkit.createButton(buttons, "Move Up", SWT.PUSH);
        moveUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    moveUpField();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button moveDown = toolkit.createButton(buttons, "Move Down", SWT.PUSH);
        moveDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveDown.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    moveDownField();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        return composite;
    }

    public void addField() throws Exception {

        AddFieldMappingWizard wizard = new AddFieldMappingWizard();
        WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
        dialog.setPageSize(600, 300);
        dialog.open();

        MappingFieldConfig fieldMapping = wizard.getFieldConfig();
        mappingConfig.addFieldConfig(fieldMapping);
        
        refresh();
        checkDirty();
    }

    public void editField() throws Exception {
        if (fieldMappings.getSelectionCount() == 0) return;

        TableItem ti = fieldMappings.getSelection()[0];
        MappingFieldConfig fieldMapping = (MappingFieldConfig)ti.getData();

        EditFieldMappingWizard wizard = new EditFieldMappingWizard(fieldMapping);
        WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
        dialog.setPageSize(600, 300);
        dialog.open();

        refresh();
        checkDirty();
    }

    public void removeField() throws Exception {
        if (fieldMappings.getSelectionCount() == 0) return;

        TableItem ti = fieldMappings.getSelection()[0];
        MappingFieldConfig fieldMapping = (MappingFieldConfig)ti.getData();

        mappingConfig.removeMappingFieldConfig(fieldMapping);

        refresh();
        checkDirty();
    }

    public void moveUpField() throws Exception {
        if (fieldMappings.getSelectionCount() == 0) return;

        TableItem ti = fieldMappings.getSelection()[0];
        MappingFieldConfig fieldMapping = (MappingFieldConfig)ti.getData();

        int i = mappingConfig.getMappingFieldConfigIndex(fieldMapping);
        if (i == 0) return;

        mappingConfig.removeMappingFieldConfig(fieldMapping);
        mappingConfig.addFieldConfig(i-1, fieldMapping);

        refresh();
        checkDirty();
    }

    public void moveDownField() throws Exception {
        if (fieldMappings.getSelectionCount() == 0) return;

        TableItem ti = fieldMappings.getSelection()[0];
        MappingFieldConfig fieldMapping = (MappingFieldConfig)ti.getData();

        int i = mappingConfig.getMappingFieldConfigIndex(fieldMapping);
        if (i == mappingConfig.getFieldConfigs().size()-1) return;

        mappingConfig.removeMappingFieldConfig(fieldMapping);
        mappingConfig.addFieldConfig(i+1, fieldMapping);

        refresh();
        checkDirty();
    }

    public void refresh() throws Exception {

        fieldMappings.removeAll();
        
        for (MappingFieldConfig fieldConfig : mappingConfig.getFieldConfigs()) {
            String value;

            Object constant = fieldConfig.getConstant();
            if (constant != null) {
                if (constant instanceof byte[]) {
                    value = "(binary)";
                } else {
                    value = "\"" + constant + "\"";
                }

            } else {
                value = fieldConfig.getVariable();
            }

            if (value == null) {
                Expression expression = fieldConfig.getExpression();
                value = expression == null ? null : expression.getScript();
            }

            boolean required = fieldConfig.isRequired();
            String condition = fieldConfig.getCondition();

            TableItem item = new TableItem(fieldMappings, SWT.NONE);
            item.setText(0, fieldConfig.getName());
            item.setText(1, value == null ? "" : value);
            item.setText(2, required ? "Yes" : "");
            item.setText(3, condition == null ? "" : condition);
            item.setData(fieldConfig);
        }
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}