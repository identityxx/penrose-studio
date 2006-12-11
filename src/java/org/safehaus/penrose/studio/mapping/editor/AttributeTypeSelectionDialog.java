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
package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class AttributeTypeSelectionDialog extends Dialog implements SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public final static int CANCEL = 0;
    public final static int OK     = 1;

    Shell shell;

    Combo objectClassCombo;
    Table attributeTable;

    private SchemaManager schemaManager;
    private Collection selections = new ArrayList();

    private int action = CANCEL;

	public AttributeTypeSelectionDialog(Shell parent, int style) {
		super(parent, style);

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

        createControl(shell);
    }

    public void open () {

        Point size = new Point(400, 300);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenrosePlugin.getImage(PenroseImage.LOGO16));
        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout(2, false));

        Label objectClassLabel = new Label(parent, SWT.NONE);
        objectClassLabel.setText("Object Class:");
        objectClassLabel.setLayoutData(new GridData(GridData.FILL));

        objectClassCombo = new Combo(parent, SWT.READ_ONLY);
        objectClassCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        objectClassCombo.addSelectionListener(this);

        attributeTable = new Table(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        attributeTable.setHeaderVisible(true);
        attributeTable.setLinesVisible(true);

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        attributeTable.setLayoutData(gd);

        TableColumn tc = new TableColumn(attributeTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(250);

        tc = new TableColumn(attributeTable, SWT.NONE);
        tc.setText("Required");
        tc.setWidth(100);

        Composite buttons = new Composite(parent, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = GridData.END;
        buttons.setLayoutData(gd);
        buttons.setLayout(new RowLayout());

		Button saveButton = new Button(buttons, SWT.PUSH);
        saveButton.setText("Select");

		saveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                TableItem items[] = attributeTable.getSelection();
                for (int i=0; i<items.length; i++) {
                    TableItem item = items[i];
                    selections.add(item.getText());
                }
                action = OK;
                shell.close();
			}
		});

		Button cancelButton = new Button(buttons, SWT.PUSH);
        cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
                shell.close();
			}
		});
	}

    public Collection getSelections() {
        return selections;
    }

    public void setSelections(Collection selections) {
        this.selections = selections;
    }

    public SchemaManager getSchemaManager() {
        return schemaManager;
    }

    public void setSchemaManager(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;

        objectClassCombo.add("");

        Collection list = sortObjectClasses(schemaManager.getObjectClasses());
        for (Iterator i=list.iterator(); i.hasNext(); ) {
            ObjectClass oc = (ObjectClass)i.next();
            objectClassCombo.add(oc.getName());
        }

        objectClassCombo.setText("");
        showAttributes("");
    }

    public void showAttributes(String ocName) {
        attributeTable.removeAll();

        if ("".equals(ocName)) {

            Collection list = sortAttributeTypes(schemaManager.getAttributeTypes());
            for (Iterator i=list.iterator(); i.hasNext(); ) {
                AttributeType at = (AttributeType)i.next();

                TableItem item = new TableItem(attributeTable, SWT.NONE);
                item.setText(0, at.getName());
                item.setText(1, "");
            }

            return;
        }

        ObjectClass oc = schemaManager.getObjectClass(ocName);
        Collection atNames = oc.getRequiredAttributes();

        for (Iterator i=atNames.iterator(); i.hasNext(); ) {
            String atName = (String)i.next();

            TableItem item = new TableItem(attributeTable, SWT.NONE);
            item.setText(0, atName);
            item.setText(1, "yes");
        }

        atNames = oc.getOptionalAttributes();

        for (Iterator i=atNames.iterator(); i.hasNext(); ) {
            String atName = (String)i.next();

            TableItem item = new TableItem(attributeTable, SWT.NONE);
            item.setText(0, atName);
            item.setText(1, "");
        }
    }

    public void widgetSelected(SelectionEvent event) {
        try {
            String ocName = objectClassCombo.getText();
            showAttributes(ocName);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public Collection sortAttributeTypes(Collection list) {
        Map map = new TreeMap();
        for (Iterator i=list.iterator(); i.hasNext(); ) {
            AttributeType at = (AttributeType)i.next();
            map.put(at.getName(), at);
        }
        return map.values();
    }

    public Collection sortObjectClasses(Collection list) {
        Map map = new TreeMap();
        for (Iterator i=list.iterator(); i.hasNext(); ) {
            ObjectClass oc = (ObjectClass)i.next();
            map.put(oc.getName(), oc);
        }
        return map.values();
    }
}
