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
package org.safehaus.penrose.studio.connector;

import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.*;
import org.safehaus.penrose.studio.parameter.ParameterDialog;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.cache.CacheConfig;
import org.safehaus.penrose.config.PenroseConfig;
import org.apache.log4j.Logger;

public class ConnectorCachePage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Text connectorNameText;
	Text connectorClassText;
	Text descriptionText;

    Table parameterTable;

    Button addButton;
    Button editButton;
    Button removeButton;

    ConnectorEditor editor;
	CacheConfig cacheConfig;

    public ConnectorCachePage(ConnectorEditor editor) {
        this.editor = editor;
    }

    public Control createControl() {
        toolkit = new FormToolkit(editor.getParent().getDisplay());

        Form form = toolkit.createForm(editor.getParent());
        form.setText("Connector Editor");

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

		connectorNameText = toolkit.createText(composite, cacheConfig.getName(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
		connectorNameText.setLayoutData(gd);

        connectorNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                cacheConfig.setName(connectorNameText.getText());
                checkDirty();
            }
        });

		Label connectorClassLabel = toolkit.createLabel(composite, "Class:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        connectorClassLabel.setLayoutData(gd);

        connectorClassText = toolkit.createText(composite, cacheConfig.getCacheClass(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        connectorClassText.setLayoutData(gd);

        connectorClassText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                cacheConfig.setCacheClass(connectorClassText.getText());
                checkDirty();
            }
        });

        Label descriptionLabel = toolkit.createLabel(composite, "Description:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        descriptionLabel.setLayoutData(gd);

        descriptionText = toolkit.createText(composite, cacheConfig.getDescription(), SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        descriptionText.setLayoutData(gd);

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                cacheConfig.setDescription(descriptionText.getText());
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
                        cacheConfig.removeParameter(oldName);
                    }

                    cacheConfig.setParameter(newName, newValue);

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

                    cacheConfig.setParameter(dialog.getName(), dialog.getValue());

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
                        cacheConfig.removeParameter(oldName);
                    }

                    cacheConfig.setParameter(newName, newValue);

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
                        cacheConfig.removeParameter(name);
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

        for (Iterator i=cacheConfig.getParameterNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            String value = cacheConfig.getParameter(name);

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
