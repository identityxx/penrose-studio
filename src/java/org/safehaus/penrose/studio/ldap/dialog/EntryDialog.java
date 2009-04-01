/**
 * Copyright 2009 Red Hat, Inc.
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
package org.safehaus.penrose.studio.ldap.dialog;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.ldap.Attribute;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.ad.ActiveDirectory;
import org.safehaus.penrose.util.BinaryUtil;

import java.util.Collection;
import java.util.HashSet;

public class EntryDialog extends Dialog {

    Logger log = Logger.getLogger(getClass());

    public final static int OK     = 0;
    public final static int SKIP   = 1;
    public final static int CANCEL = 2;

    Shell shell;

	Text dnText;
    Table attributesTable;

    private DN dn;
    private Attributes attributes;

    int action;

    public Collection<String> guidAttributes = new HashSet<String>();
    public Collection<String> sidAttributes = new HashSet<String>();

    public EntryDialog(Shell parent, int style) {
        super(parent, style);
    }

    public int open() {

        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
        createControl(shell);
        load();
        
        Point size = new Point(600, 400);
        shell.setSize(size);

        Point l = getParent().getLocation();
        Point s = getParent().getSize();

        shell.setLocation(l.x + (s.x - size.x)/2, l.y + (s.y - size.y)/2);

        shell.setText(getText());
        shell.setImage(PenroseStudio.getImage(PenroseImage.LOGO));

        shell.open();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }

        return action;
    }

    public void createControl(final Shell parent) {
        parent.setLayout(new GridLayout());

        Composite dnPanel = createDNPanel(parent);
        dnPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        new Label(parent, SWT.NONE);

        Composite attributesPanel = createAttributesPanel(parent);
        attributesPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttonsPanel = createButtonsPanel(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.END;
        buttonsPanel.setLayoutData(gd);
    }

    public Composite createDNPanel(final Shell parent) {

        Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Label dnLabel = new Label(composite, SWT.NONE);
        dnLabel.setText("DN:");

		dnText = new Text(composite, SWT.BORDER);
		dnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createAttributesPanel(final Shell parent) {

        Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

        attributesTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 200;
        attributesTable.setLayoutData(gd);

        attributesTable.setHeaderVisible(true);
        attributesTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(attributesTable, SWT.NONE);
        tc.setText("Attribute");
        tc.setWidth(150);

        tc = new TableColumn(attributesTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(300);

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayout(new GridLayout());
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("  Add  ");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
                    AttributeDialog dialog = new AttributeDialog(parent, SWT.NONE);
                    dialog.setText("Add Attribute");

                    if (dialog.open() == AttributeDialog.CANCEL) return;

                    TableItem ti = new TableItem(attributesTable, SWT.NONE);
                    setTableItem(ti, dialog.getName(), dialog.getValue());

				} catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
					ErrorDialog.open(ex);
				}
			}
		});

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setText("  Edit  ");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    if (attributesTable.getSelectionCount() == 0) return;
                    TableItem ti = attributesTable.getSelection()[0];

                    AttributeDialog dialog = new AttributeDialog(parent, SWT.NONE);
                    dialog.setText("Edit Attribute");
                    dialog.setName(ti.getText(0));
                    dialog.setValue(ti.getData());
                    
                    if (dialog.open() == AttributeDialog.CANCEL) return;

                    setTableItem(ti, dialog.getName(), dialog.getValue());

                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                    ErrorDialog.open(ex);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("  Remove  ");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    if (attributesTable.getSelectionCount() == 0) return;
                    TableItem ti = attributesTable.getSelection()[0];
                    ti.dispose();

                } catch (Exception ex) {
                    log.debug(ex.getMessage(), ex);
                    ErrorDialog.open(ex);
                }
            }
        });

        return buttons;
    }

    public Composite createButtonsPanel(final Shell parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new RowLayout());

        Button saveButton = new Button(composite, SWT.PUSH);
        saveButton.setText("  OK  ");

        saveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                store();
                action = OK;
                shell.close();
            }
        });

        Button skipButton = new Button(composite, SWT.PUSH);
        skipButton.setText("  Skip  ");

        skipButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                action = SKIP;
                shell.close();
            }
        });

        Button cancelButton = new Button(composite, SWT.PUSH);
        cancelButton.setText("  Cancel  ");

        cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                action = CANCEL;
                shell.close();
            }
        });

        return composite;
    }

	public void store() {
        dn = new DN(dnText.getText().trim());

        attributes = new Attributes();
        for (TableItem ti : attributesTable.getItems()) {
            String name = ti.getText(0);
            Object value = ti.getData();
            attributes.addValue(name, value);
        }
    }

    public void load() {
        dnText.setText(dn.toString());

        attributesTable.removeAll();
        for (Attribute attribute : attributes.getAll()) {
            String name = attribute.getName();

            for (Object value : attribute.getValues()) {

                TableItem ti = new TableItem(attributesTable, SWT.NONE);
                setTableItem(ti, name, value);
            }
        }
    }

    public void setTableItem(TableItem ti, String name, Object value) {

        ti.setText(0, name);

        String normalizedName = name.toLowerCase();
        String s;

        if (guidAttributes.contains(normalizedName)) {
            if (value instanceof String) {
                s = ActiveDirectory.getGUID(((String)value).getBytes());

            } else if (value instanceof byte[]) {
                s = ActiveDirectory.getGUID((byte[])value);

            } else {
                s = value.toString();
            }

        } else if (sidAttributes.contains(normalizedName)) {
            if (value instanceof String) {
                s = ActiveDirectory.getSID(((String)value).getBytes());

            } else if (value instanceof byte[]) {
                s = ActiveDirectory.getSID((byte[])value);

            } else {
                s = value.toString();
            }


        } else if (value instanceof byte[]) {
            s = BinaryUtil.encode(BinaryUtil.BIG_INTEGER, (byte[])value);

        } else {
            s = value.toString();
        }

        ti.setText(1, s);
        ti.setData(value);
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public DN getDn() {
        return dn;
    }

    public void setDn(DN dn) {
        this.dn = dn;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public void setGUIDAttribute(Collection<String> guidAttributes) {
        this.guidAttributes.clear();
        this.guidAttributes.addAll(guidAttributes);
    }

    public void setSIDAttribute(Collection<String> guidAttributes) {
        this.sidAttributes.clear();
        this.sidAttributes.addAll(sidAttributes);
    }
}