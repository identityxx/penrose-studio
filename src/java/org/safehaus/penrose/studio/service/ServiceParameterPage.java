/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.studio.service;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.studio.parameter.ParameterDialog;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class ServiceParameterPage extends WizardPage implements SelectionListener, ModifyListener {

    public final static String NAME = "Parameters";

    Table parameterTable;

    public ServiceParameterPage() {
        super(NAME);
        setDescription("Add service parameters.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        parameterTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        parameterTable.setHeaderVisible(true);
        parameterTable.setLinesVisible(true);
        parameterTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(parameterTable, SWT.NONE);
        tc.setText("Parameter");
        tc.setWidth(200);

        tc = new TableColumn(parameterTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(200);

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

                TableItem item = new TableItem(parameterTable, SWT.NONE);
                item.setText(0, dialog.getName());
                item.setText(1, dialog.getValue());
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                TableItem items[] = parameterTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    TableItem item = items[i];
                    item.dispose();
                }
            }
        });

        setPageComplete(validatePage());
    }

    public Map getParameters() {
        Map parameters = new TreeMap();
        TableItem items[] = parameterTable.getItems();
        for (int i=0; i<items.length; i++) {
            TableItem item = items[i];
            parameters.put(item.getText(0), item.getText(1));
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
