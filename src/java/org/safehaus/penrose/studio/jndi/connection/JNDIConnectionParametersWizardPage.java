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
package org.safehaus.penrose.studio.jndi.connection;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.studio.parameter.ParameterDialog;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class JNDIConnectionParametersWizardPage extends WizardPage implements ModifyListener {

    public final static String NAME = "Connection Parameters";

    Table parametersTable;

    Map<String,String> parameters = new TreeMap<String,String>();

    public JNDIConnectionParametersWizardPage() {
        super(NAME);
        setDescription("Enter connection parameters.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        parametersTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        parametersTable.setHeaderVisible(true);
        parametersTable.setLinesVisible(true);
        parametersTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(250);

        tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(250);

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                dialog.setText("Add parameter...");
                dialog.open();

                if (dialog.getAction() == ParameterDialog.CANCEL) return;

                parameters.put(dialog.getName(), dialog.getValue());

                refresh();
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Delete");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (parametersTable.getSelectionCount() == 0) return;

                TableItem items[] = parametersTable.getSelection();
                for (TableItem item : items) {
                    String name = item.getText(0);
                    parameters.remove(name);
                }

                refresh();
            }
        });

        refresh();

        setPageComplete(validatePage());
    }

    public void setParameters(Map<String,String> parameters) {
        this.parameters.putAll(parameters);
    }

    public Map<String,String> getParameters() {
        return parameters;
    }

    public void refresh() {
        parametersTable.removeAll();

        for (String name : parameters.keySet()) {
            String value = (String) parameters.get(name);

            TableItem ti = new TableItem(parametersTable, SWT.NONE);
            ti.setText(0, name);
            ti.setText(1, value);
        }
    }

    public boolean validatePage() {
        return true;
    }

    public void modifyText(ModifyEvent event) {
        setPageComplete(validatePage());
    }
}
