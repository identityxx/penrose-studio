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
package org.safehaus.penrose.studio.mapping.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.FillLayout;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.SchemaManager;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class ObjectClassWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Object Classes";

    Table availableTable;
    Table selectedTable;

    Button addButton;
    Button removeButton;
    Button addAllButton;
    Button removeAllButton;

    Project project;
    Collection<String> availableOCs = new TreeSet<String>();
    Collection<String> selectedOCs = new TreeSet<String>();

    public ObjectClassWizardPage(Project project) {
        super(NAME);

        this.project = project;
        setDescription("Select object classes.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 3;
        composite.setLayout(sectionLayout);

        availableTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        availableTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setSize(50, 100);
        buttons.setLayout(new FillLayout(SWT.VERTICAL));

        addButton = new Button(buttons, SWT.PUSH);
        addButton.setText(">");
        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (availableTable.getSelectionCount() == 0) return;

                SchemaManager schemaManager = project.getSchemaManager();

                TableItem items[] = availableTable.getSelection();
                for (TableItem item : items) {
                    String objectClass = (String) item.getData();

                    Collection<String> ocNames = schemaManager.getAllObjectClassNames(objectClass);
                    for (String ocName : ocNames) {
                        availableOCs.remove(ocName);
                        selectedOCs.add(ocName);
                    }
                }

                refresh();
            }
        });

        removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("<");
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (selectedTable.getSelectionCount() == 0) return;

                SchemaManager schemaManager = project.getSchemaManager();

                TableItem items[] = selectedTable.getSelection();
                for (TableItem item : items) {
                    String objectClass = (String) item.getData();
                    selectedOCs.remove(objectClass);
                    availableOCs.add(objectClass);
                }

                Collection<String> list = new ArrayList<String>();

                for (String objectClass : selectedOCs) {

                    boolean missingSuperclass = false;
                    Collection<String> ocNames = schemaManager.getAllObjectClassNames(objectClass);
                    for (String ocName : ocNames) {
                        if (selectedOCs.contains(ocName)) continue;
                        missingSuperclass = true;
                        break;
                    }

                    if (!missingSuperclass) continue;

                    list.add(objectClass);
                }

                for (String objectClass : list) {
                    selectedOCs.remove(objectClass);
                    availableOCs.add(objectClass);
                }

                refresh();
            }
        });

        addAllButton = new Button(buttons, SWT.PUSH);
        addAllButton.setText(">>");
        addAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                for (String objectClass : availableOCs) {
                    selectedOCs.add(objectClass);
                }
                availableOCs.clear();

                refresh();
            }
        });

        removeAllButton = new Button(buttons, SWT.PUSH);
        removeAllButton.setText("<<");
        removeAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                for (String objectClass : selectedOCs) {
                    availableOCs.add(objectClass);
                }
                selectedOCs.clear();

                refresh();
            }
        });

        selectedTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        selectedTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        setPageComplete(validatePage());
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) init();
    }

    public void init() {
        try {
            Schema schema = project.getSchemaManager().getAllSchema();

            Collection<String> ocNames = new ArrayList<String>();
            for (ObjectClass objectClass : schema.getObjectClasses()) {
                ocNames.add(objectClass.getName());
            }

            availableOCs.addAll(ocNames);
            availableOCs.removeAll(selectedOCs);

            refresh();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void refresh() {
        availableTable.removeAll();
        selectedTable.removeAll();

        for (String objectClass : availableOCs) {
            TableItem item = new TableItem(availableTable, SWT.NONE);
            item.setText(objectClass);
            item.setData(objectClass);
        }

        log.debug("Object classes:");
        for (String objectClass : selectedOCs) {
            log.debug(" - " + objectClass);

            TableItem item = new TableItem(selectedTable, SWT.NONE);
            item.setText(objectClass);
            item.setData(objectClass);
        }

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        return !selectedOCs.isEmpty();
    }

    public void setSelecteObjectClasses(Collection<String> list) {
        selectedOCs.addAll(list);
    }

    public Collection getSelectedObjectClasses() {
        return selectedOCs;
    }
}
