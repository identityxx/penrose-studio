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
package org.safehaus.penrose.studio.module.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.studio.parameter.ParameterDialog;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Endi S. Dewata
 */
public class ModuleParameterWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Module Parameters";

    Table parametersTable;

    public ModuleParameterWizardPage() {
        super(NAME);
        setDescription("Enter the initialization parameters.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        parametersTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        parametersTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        parametersTable.setHeaderVisible(true);
        parametersTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Parameter");
        tc.setWidth(150);

        tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(300);

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addButton.setText("Add");

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add parameter...");
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    TableItem item = new TableItem(parametersTable, SWT.NONE);
                    item.setText(0, dialog.getName());
                    item.setText(1, dialog.getValue());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        editButton.setText("Edit");

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parametersTable.getSelectionCount() == 0) return;

                    TableItem item = parametersTable.getSelection()[0];

                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Edit parameter...");
                    dialog.setName(item.getText(0));
                    dialog.setValue(item.getText(1));
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    item.setText(0, dialog.getName());
                    item.setText(1, dialog.getValue());
                    parametersTable.redraw();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        removeButton.setText("Remove");

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parametersTable.getSelectionCount() == 0) return;

                    TableItem item = parametersTable.getSelection()[0];
                    item.dispose();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        setPageComplete(validatePage());
    }

    public Map<String,String> getParameters() {
        Map<String,String> parameters = new HashMap<String,String>();
        TableItem items[] = parametersTable.getItems();
        for (TableItem item : items) {
            String name = item.getText(0);
            String value = item.getText(1);
            parameters.put(name, value);
        }
        return parameters;
    }

    public boolean validatePage() {
        return true;
    }

    public void widgetSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
