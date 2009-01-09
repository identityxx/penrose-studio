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
package org.safehaus.penrose.studio.driver;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.studio.driver.ParameterDialog;
import org.safehaus.penrose.studio.driver.DriverWizard;
import org.safehaus.penrose.config.Parameter;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.List;

/**
 * @author Endi S. Dewata
 */
public class DriverParameterPage extends WizardPage implements SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Driver Parameters";

    List parameters = new ArrayList();
    Table parameterTable;

    public DriverParameterPage() {
        super(NAME);
        setDescription("Add driver parameters.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        Label adapterLabel = new Label(composite, SWT.NONE);
        adapterLabel.setText("Parameters:");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        adapterLabel.setLayoutData(gd);

        parameterTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        parameterTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        parameterTable.addSelectionListener(this);

        parameterTable.setHeaderVisible(true);
        parameterTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(parameterTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(150);

        tc = new TableColumn(parameterTable, SWT.NONE);
        tc.setText("Display Name");
        tc.setWidth(150);

        tc = new TableColumn(parameterTable, SWT.NONE);
        tc.setText("Default Value");
        tc.setWidth(150);

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    Parameter parameter = new Parameter();

                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add Parameter...");
                    dialog.setParameter(parameter);
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    addParameter(parameter);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parameterTable.getSelectionCount() == 0) return;

                    TableItem item = parameterTable.getSelection()[0];
                    Parameter parameter = (Parameter)item.getData();

                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Edit Parameter...");
                    dialog.setParameter(parameter);
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parameterTable.getSelectionCount() == 0) return;

                    TableItem item = parameterTable.getSelection()[0];
                    Parameter parameter = (Parameter)item.getData();

                    removeParameter(parameter);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        new Label(buttons, SWT.NONE);

        Button moveUpButton = new Button(buttons, SWT.PUSH);
        moveUpButton.setText("Move Up");
        moveUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parameterTable.getSelectionCount() == 0) return;

                    TableItem item = parameterTable.getSelection()[0];
                    Parameter parameter = (Parameter)item.getData();

                    moveUp(parameter);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Button moveDownButton = new Button(buttons, SWT.PUSH);
        moveDownButton.setText("Move Down");
        moveDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveDownButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parameterTable.getSelectionCount() == 0) return;

                    TableItem item = parameterTable.getSelection()[0];
                    Parameter parameter = (Parameter)item.getData();

                    moveDown(parameter);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        refresh();

        setPageComplete(validatePage());
    }

    public void setVisible(boolean visible) {
        System.out.println("[DriverParameterPage] setVisible: "+visible);
        super.setVisible(visible);

        if (visible) init();
    }

    public void init() {
        try {
            DriverWizard wizard = (DriverWizard)getWizard();
            Driver driver = wizard.getDriver();
            if (driver == null) return;

            parameters.clear();
            parameters.addAll(driver.getParameters());
            refresh();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void refresh() {
        parameterTable.removeAll();
        for (Iterator i=parameters.iterator(); i.hasNext(); ) {
            Parameter parameter = (Parameter)i.next();

            TableItem item = new TableItem(parameterTable, SWT.NONE);
            item.setText(0, parameter.getName());
            item.setText(1, parameter.getDisplayName());
            item.setText(2, parameter.getDefaultValue() == null ? "" : parameter.getDefaultValue());
            item.setData(parameter);
        }
    }

    public void addParameter(Parameter parameter) {
        System.out.println("Adding parameter: "+parameter);
        if (parameters.contains(parameter)) return;
        parameters.add(parameter);
        System.out.println("Added parameter: "+parameter);
        refresh();
    }

    public void removeParameter(Parameter parameter) {
        parameters.remove(parameter);
        refresh();
    }

    public void moveUp(Parameter parameter) {
        int i = parameters.indexOf(parameter);
        if (i <= 0) return;
        parameters.remove(parameter);
        parameters.add(i-1, parameter);
        refresh();
        parameterTable.setSelection(i-1);
    }

    public void moveDown(Parameter parameter) {
        int i = parameters.indexOf(parameter);
        if (i >= parameters.size()-1) return;
        parameters.remove(parameter);
        parameters.add(i+1, parameter);
        refresh();
        parameterTable.setSelection(i+1);
    }

    public Collection getParameters() {
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
}
