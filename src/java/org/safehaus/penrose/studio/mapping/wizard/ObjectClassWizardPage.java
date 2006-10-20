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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.object.ObjectsView;
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

    Collection availableOCs = new TreeSet();
    Collection selectedOCs = new TreeSet();

    public ObjectClassWizardPage() {
        super(NAME);

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
                try {
                    if (availableTable.getSelectionCount() == 0) return;

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    IWorkbenchPage page = window.getActivePage();
                    ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

                    ServerNode serverNode = objectsView.getSelectedProjectNode();
                    if (serverNode == null) return;

                    Server server = serverNode.getProject();
                    SchemaManager schemaManager = server.getSchemaManager();

                    TableItem items[] = availableTable.getSelection();
                    for (int i=0; i<items.length; i++) {
                        String objectClass = (String)items[i].getData();

                        Collection ocNames = schemaManager.getAllObjectClassNames(objectClass);
                        for (Iterator j=ocNames.iterator(); j.hasNext(); ) {
                            String ocName = (String)j.next();
                            availableOCs.remove(ocName);
                            selectedOCs.add(ocName);
                        }
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("<");
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (selectedTable.getSelectionCount() == 0) return;

                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    IWorkbenchPage page = window.getActivePage();
                    ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

                    ServerNode serverNode = objectsView.getSelectedProjectNode();
                    if (serverNode == null) return;

                    Server server = serverNode.getProject();
                    SchemaManager schemaManager = server.getSchemaManager();

                    TableItem items[] = selectedTable.getSelection();
                    for (int i=0; i<items.length; i++) {
                        String objectClass = (String)items[i].getData();
                        selectedOCs.remove(objectClass);
                        availableOCs.add(objectClass);
                    }

                    Collection list = new ArrayList();

                    for (Iterator i=selectedOCs.iterator(); i.hasNext(); ) {
                        String objectClass = (String)i.next();

                        boolean missingSuperclass = false;
                        Collection ocNames = schemaManager.getAllObjectClassNames(objectClass);
                        for (Iterator j=ocNames.iterator(); j.hasNext(); ) {
                            String ocName = (String)j.next();
                            if (selectedOCs.contains(ocName)) continue;
                            missingSuperclass = true;
                            break;
                        }

                        if (!missingSuperclass) continue;

                        list.add(objectClass);
                    }

                    for (Iterator i=list.iterator(); i.hasNext(); ) {
                        String objectClass = (String)i.next();
                        selectedOCs.remove(objectClass);
                        availableOCs.add(objectClass);
                    }

                    refresh();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        addAllButton = new Button(buttons, SWT.PUSH);
        addAllButton.setText(">>");
        addAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                for (Iterator i=availableOCs.iterator(); i.hasNext(); ) {
                    String objectClass = (String)i.next();
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
                for (Iterator i=selectedOCs.iterator(); i.hasNext(); ) {
                    String objectClass = (String)i.next();
                    availableOCs.add(objectClass);
                }
                selectedOCs.clear();

                refresh();
            }
        });

        selectedTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        selectedTable.setLayoutData(new GridData(GridData.FILL_BOTH));

    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) init();
    }

    public void init() {
        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            ServerNode serverNode = objectsView.getSelectedProjectNode();
            if (serverNode == null) return;

            Server server = serverNode.getProject();
            Schema schema = server.getSchemaManager().getAllSchema();

            Collection ocNames = new ArrayList();
            for (Iterator i=schema.getObjectClasses().iterator(); i.hasNext(); ) {
                ObjectClass objectClass = (ObjectClass)i.next();
                ocNames.add(objectClass.getName());
            }
            
            availableOCs.addAll(ocNames);
            availableOCs.removeAll(selectedOCs);

            refresh();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void refresh() {
        availableTable.removeAll();
        selectedTable.removeAll();

        for (Iterator i=availableOCs.iterator(); i.hasNext(); ) {
            String objectClass = (String)i.next();

            TableItem item = new TableItem(availableTable, SWT.NONE);
            item.setText(objectClass);
            item.setData(objectClass);
        }

        for (Iterator i=selectedOCs.iterator(); i.hasNext(); ) {
            String objectClass = (String)i.next();

            TableItem item = new TableItem(selectedTable, SWT.NONE);
            item.setText(objectClass);
            item.setData(objectClass);
        }
    }

    public void setSelecteObjectClasses(Collection list) {
        selectedOCs.addAll(list);
    }

    public Collection getSelectedObjectClasses() {
        return selectedOCs;
    }
}
