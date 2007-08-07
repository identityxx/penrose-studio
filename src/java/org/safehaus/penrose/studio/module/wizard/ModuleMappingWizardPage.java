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
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.studio.module.ModuleMappingDialog;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class ModuleMappingWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Module Mappings";

    Table mappingsTable;

    public ModuleMappingWizardPage() {
        super(NAME);
        setDescription("Enter module mappings.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 2;
        composite.setLayout(sectionLayout);

        mappingsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        mappingsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        mappingsTable.setHeaderVisible(true);
        mappingsTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(mappingsTable, SWT.NONE);
        tc.setText("Base DN");
        tc.setWidth(200);

        tc = new TableColumn(mappingsTable, SWT.NONE);
        tc.setText("Scope");
        tc.setWidth(100);

        tc = new TableColumn(mappingsTable, SWT.NONE);
        tc.setText("Filter");
        tc.setWidth(150);

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addButton.setText("Add");

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
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

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        editButton.setText("Edit");

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (mappingsTable.getSelectionCount() == 0) return;

                    TableItem item = mappingsTable.getSelection()[0];
                    ModuleMapping mapping = (ModuleMapping)item.getData();

                    ModuleMappingDialog dialog = new ModuleMappingDialog(parent.getShell(), SWT.NONE);
                    dialog.setMapping(mapping);
                    dialog.open();

                    if (dialog.getAction() == ModuleMappingDialog.CANCEL) return;

                    item.setText(0, mapping.getBaseDn().toString());
                    item.setText(1, mapping.getScope());
                    item.setText(2, mapping.getFilter());
                    mappingsTable.redraw();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        removeButton.setText("Remove");

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (mappingsTable.getSelectionCount() == 0) return;

                    TableItem item = mappingsTable.getSelection()[0];
                    item.dispose();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        setPageComplete(validatePage());
    }

    public Collection getMappings() {
        Collection mappings = new ArrayList();
        TableItem items[] = mappingsTable.getItems();
        for (int i=0; i<items.length; i++) {
        	ModuleMapping moduleMapping = (ModuleMapping)items[i].getData();
            mappings.add(moduleMapping);
        }
        return mappings;
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
