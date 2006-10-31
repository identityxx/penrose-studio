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
package org.safehaus.penrose.studio.partition.editor;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.safehaus.penrose.studio.parameter.ParameterDialog;
import org.safehaus.penrose.cache.CacheConfig;

public class PartitionCachePage extends PartitionEditorPage {

    Text cacheClassText;
    Text descriptionText;

    Table parametersTable;

    CacheConfig cacheConfig;

    public PartitionCachePage(PartitionEditor editor, String name, String label, CacheConfig cacheConfig) {
        super(editor, name, label);

        this.cacheConfig = cacheConfig;
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

        String cacheClass = cacheConfig.getCacheClass() == null ? "" : cacheConfig.getCacheClass();
        cacheClassText.setText(cacheClass);

        String description = cacheConfig.getDescription() == null ? "" : cacheConfig.getDescription();
        descriptionText.setText(description);

        parametersTable.removeAll();

        for (Iterator i=cacheConfig.getParameterNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            String value = cacheConfig.getParameter(name);

            TableItem tableItem = new TableItem(parametersTable, SWT.NONE);
            tableItem.setText(0, name);
            tableItem.setText(1, value);
        }

    }

    public Composite createPropertiesControl(final Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label cacheClassLabel = getToolkit().createLabel(composite, "Class Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        cacheClassLabel.setLayoutData(gd);

        cacheClassText = getToolkit().createText(composite, "", SWT.BORDER);
        cacheClassText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        cacheClassText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                cacheConfig.setCacheClass("".equals(cacheClassText.getText()) ? null : cacheClassText.getText());
                checkDirty();
            }
        });

        Label descriptionLabel = getToolkit().createLabel(composite, "Description:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        descriptionLabel.setLayoutData(gd);

        descriptionText = getToolkit().createText(composite, "", SWT.BORDER);
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                cacheConfig.setDescription("".equals(descriptionText.getText()) ? null : descriptionText.getText());
                checkDirty();
            }
        });

        return composite;
    }

    public Composite createParametersControl(final Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        parametersTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        parametersTable.setHeaderVisible(true);
        parametersTable.setLinesVisible(true);

        GridData gd = new GridData(GridData.FILL_BOTH);
        parametersTable.setLayoutData(gd);

        TableColumn tc = new TableColumn(parametersTable, SWT.LEFT);
        tc.setText("Name");
        tc.setWidth(250);

        tc = new TableColumn(parametersTable, SWT.LEFT);
        tc.setText("Value");
        tc.setWidth(250);

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
                        cacheConfig.removeParameter(oldName);
                    }

                    cacheConfig.setParameter(newName, newValue);

                    refresh();
                    parametersTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Composite buttons = getToolkit().createComposite(composite);
        gd = new GridData(GridData.FILL_VERTICAL);
        buttons.setLayoutData(gd);
        buttons.setLayout(new GridLayout());

        Button addButton = getToolkit().createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add parameter...");
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    cacheConfig.setParameter(dialog.getName(), dialog.getValue());

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
                        cacheConfig.removeParameter(oldName);
                    }

                    cacheConfig.setParameter(newName, newValue);

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
                        cacheConfig.removeParameter(name);
                    }

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        refresh();

        return composite;
    }
}
