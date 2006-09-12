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
package org.safehaus.penrose.studio.source.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.util.JNDIClient;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.partition.ConnectionConfig;
import org.safehaus.penrose.partition.Partition;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class JNDIAttributeWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "LDAP Attributes";

    Table availableAttrTable;
    Table selectedAttrTable;

    Button addButton;
    Button removeButton;
    Button addAllButton;
    Button removeAllButton;

    Combo objectClassCombo;

    ConnectionConfig xconnectionConfig;
    Collection attributeNames;
    Schema schema;

    public JNDIAttributeWizardPage() throws Exception {
        this(new ArrayList());
    }

    public JNDIAttributeWizardPage(Collection attributeNames) throws Exception {
        super(NAME);

        this.attributeNames = attributeNames;

        setDescription("Select LDAP attributes.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        GridLayout sectionLayout = new GridLayout();
        sectionLayout.numColumns = 3;
        composite.setLayout(sectionLayout);

        Label objectClassLabel = new Label(composite, SWT.NONE);
        objectClassLabel.setText("Object Class:");
        GridData gd = new GridData(GridData.FILL);
        gd.horizontalSpan = 3;
        objectClassLabel.setLayoutData(gd);

        objectClassCombo = new Combo(composite, SWT.READ_ONLY);
        objectClassCombo.add("");
        objectClassCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        objectClassCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                String ocName = objectClassCombo.getText();
                if ("".equals(ocName)) {
                    showAllAttributes();
                    return;
                }

                showObjectClassAttributes(ocName);
            }
        });

        Label separator = new Label(composite, SWT.NONE);
        gd = new GridData(GridData.FILL);
        gd.horizontalSpan = 3;
        separator.setLayoutData(gd);

        Label adapterLabel = new Label(composite, SWT.NONE);
        adapterLabel.setText("Available:");

        new Label(composite, SWT.NONE);

        Label infoLabel = new Label(composite, SWT.NONE);
        infoLabel.setText("Selected:");

        availableAttrTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        availableAttrTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setSize(50, 100);
        buttons.setLayout(new FillLayout(SWT.VERTICAL));

        addButton = new Button(buttons, SWT.PUSH);
        addButton.setText(">");
        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (availableAttrTable.getSelectionCount() == 0) return;

                Map map = new TreeMap();
                TableItem items[] = selectedAttrTable.getItems();
                for (int i=0; i<items.length; i++) {
                    AttributeType attrType = (AttributeType)items[i].getData();
                    map.put(attrType.getName(), attrType);
                }

                items = availableAttrTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    AttributeType attrType = (AttributeType)items[i].getData();
                    map.put(attrType.getName(), attrType);
                    items[i].dispose();
                }

                selectedAttrTable.removeAll();
                for (Iterator i=map.values().iterator(); i.hasNext(); ) {
                    AttributeType attrType = (AttributeType)i.next();
                    TableItem item = new TableItem(selectedAttrTable, SWT.NONE);
                    item.setText(attrType.getName());
                    item.setData(attrType);
                }

                setPageComplete(validatePage());
            }
        });

        removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("<");
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (selectedAttrTable.getSelectionCount() == 0) return;

                Map map = new TreeMap();
                TableItem items[] = availableAttrTable.getItems();
                for (int i=0; i<items.length; i++) {
                    AttributeType attrType = (AttributeType)items[i].getData();
                    map.put(attrType.getName(), attrType);
                }

                items = selectedAttrTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    AttributeType attrType = (AttributeType)items[i].getData();
                    map.put(attrType.getName(), attrType);
                    items[i].dispose();
                }

                availableAttrTable.removeAll();
                for (Iterator i=map.values().iterator(); i.hasNext(); ) {
                    AttributeType attrType = (AttributeType)i.next();
                    TableItem item = new TableItem(availableAttrTable, SWT.NONE);
                    item.setText(attrType.getName());
                    item.setData(attrType);
                }

                setPageComplete(validatePage());
            }
        });

        addAllButton = new Button(buttons, SWT.PUSH);
        addAllButton.setText(">>");
        addAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                Map map = new TreeMap();
                TableItem items[] = selectedAttrTable.getItems();
                for (int i=0; i<items.length; i++) {
                    AttributeType attrType = (AttributeType)items[i].getData();
                    map.put(attrType.getName(), attrType);
                }

                items = availableAttrTable.getItems();
                for (int i=0; i<items.length; i++) {
                    AttributeType attrType = (AttributeType)items[i].getData();
                    map.put(attrType.getName(), attrType);
                    items[i].dispose();
                }

                selectedAttrTable.removeAll();
                for (Iterator i=map.values().iterator(); i.hasNext(); ) {
                    AttributeType attrType = (AttributeType)i.next();
                    TableItem item = new TableItem(selectedAttrTable, SWT.NONE);
                    item.setText(attrType.getName());
                    item.setData(attrType);
                }

                setPageComplete(validatePage());
            }
        });

        removeAllButton = new Button(buttons, SWT.PUSH);
        removeAllButton.setText("<<");
        removeAllButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                Map map = new TreeMap();
                TableItem items[] = availableAttrTable.getItems();
                for (int i=0; i<items.length; i++) {
                    AttributeType attrType = (AttributeType)items[i].getData();
                    map.put(attrType.getName(), attrType);
                }

                items = selectedAttrTable.getItems();
                for (int i=0; i<items.length; i++) {
                    AttributeType attrType = (AttributeType)items[i].getData();
                    map.put(attrType.getName(), attrType);
                    items[i].dispose();
                }

                availableAttrTable.removeAll();
                for (Iterator i=map.values().iterator(); i.hasNext(); ) {
                    AttributeType attrType = (AttributeType)i.next();
                    TableItem item = new TableItem(availableAttrTable, SWT.NONE);
                    item.setText(attrType.getName());
                    item.setData(attrType);
                }

                setPageComplete(validatePage());
            }
        });

        selectedAttrTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        selectedAttrTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        setPageComplete(validatePage());
    }

    public void setConnectionConfig(Partition partition, ConnectionConfig connectionConfig) {
        try {
            if (schema == null) {
                JNDIClient client = new JNDIClient(connectionConfig.getParameters());
                schema = client.getSchema();
            }

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) init();
    }

    public void init() {
        try {
            for (Iterator i=attributeNames.iterator(); i.hasNext(); ) {
                String atName = (String)i.next();
                AttributeType attrType = schema.getAttributeType(atName);

                TableItem item = new TableItem(selectedAttrTable, SWT.NONE);
                item.setText(atName);
                item.setData(attrType);
            }

            Collection ocNames = schema.getObjectClassNames();
            for (Iterator i=ocNames.iterator(); i.hasNext(); ) {
                String ocName = (String)i.next();
                objectClassCombo.add(ocName);
            }

            showAllAttributes();
            setPageComplete(validatePage());

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void showAllAttributes() {
        objectClassCombo.select(0);

        Map map = new TreeMap();

        // add attribute types from schema
        Collection attributeTypes = schema.getAttributeTypes();
        for (Iterator i=attributeTypes.iterator(); i.hasNext(); ) {
            AttributeType attrType = (AttributeType)i.next();
            map.put(attrType.getName(), attrType);
        }

        // remove attribute types already selected
        TableItem items[] = selectedAttrTable.getItems();
        for (int i=0; i<items.length; i++) {
            AttributeType attrType = (AttributeType)items[i].getData();
            map.remove(attrType.getName());
        }

        // update attribute types table
        availableAttrTable.removeAll();
        for (Iterator i=map.values().iterator(); i.hasNext(); ) {
            AttributeType attrType = (AttributeType)i.next();

            TableItem item = new TableItem(availableAttrTable, SWT.NONE);
            item.setText(attrType.getName());
            item.setData(attrType);
        }
    }

    public void showObjectClassAttributes(String ocName) {
        Map map = new TreeMap();

        // get all parent object classes
        Collection ocs = schema.getAllObjectClasses(ocName);
        for (Iterator i=ocs.iterator(); i.hasNext(); ) {
            ObjectClass objectClass = (ObjectClass)i.next();

            // add required attributes
            Collection reqAttrs = objectClass.getRequiredAttributes();
            for (Iterator j=reqAttrs.iterator(); j.hasNext(); ) {
                String atName = (String)j.next();
                AttributeType attrType = schema.getAttributeType(atName);
                map.put(attrType.getName(), attrType);
            }

            // add optional attributes
            Collection optAttrs = objectClass.getOptionalAttributes();
            for (Iterator j=optAttrs.iterator(); j.hasNext(); ) {
                String atName = (String)j.next();
                AttributeType attrType = schema.getAttributeType(atName);
                map.put(attrType.getName(), attrType);
            }
        }

        // remove attribute types already selected
        TableItem items[] = selectedAttrTable.getItems();
        for (int i=0; i<items.length; i++) {
            AttributeType attrType = (AttributeType)items[i].getData();
            map.remove(attrType.getName());
        }

        // update attribute types table
        availableAttrTable.removeAll();
        for (Iterator i=map.values().iterator(); i.hasNext(); ) {
            AttributeType attrType = (AttributeType)i.next();

            TableItem item = new TableItem(availableAttrTable, SWT.NONE);
            item.setText(attrType.getName());
            item.setData(attrType);
        }
    }

    public Collection getAttributeTypes() {
        Collection result = new ArrayList();
        TableItem items[] = selectedAttrTable.getItems();
        for (int i=0; i<items.length; i++) {
            AttributeType attrType = (AttributeType)items[i].getData();
            result.add(attrType);
        }
        return result;
    }

    public boolean validatePage() {
        return true;
    }
}
