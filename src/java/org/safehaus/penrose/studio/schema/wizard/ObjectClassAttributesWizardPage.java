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
package org.safehaus.penrose.studio.schema.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.safehaus.penrose.studio.schema.dialog.SelectAttributeTypeDialog;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.client.PenroseClient;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.TreeSet;

/**
 * @author Endi S. Dewata
 */
public class ObjectClassAttributesWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Attributes";

	Table requiredAttributesTable;
    Table optionalAttributesTable;

    Server server;
    Collection<String> requiredAttributes = new TreeSet<String>();
    Collection<String> optionalAttributes = new TreeSet<String>();

	public ObjectClassAttributesWizardPage() {
		super(NAME);
        setDescription("Enter the required and optional attributes.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label requiredLabel = new Label(composite, SWT.NONE);
        requiredLabel.setText("Required:");
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        requiredLabel.setLayoutData(gd);

        requiredAttributesTable = new Table(composite, SWT.BORDER|SWT.FULL_SELECTION);
        requiredAttributesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    SelectAttributeTypeDialog dialog = new SelectAttributeTypeDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add attributes...");

                    PenroseClient client = server.getClient();
                    dialog.setSchemaManagerClient(client.getSchemaManagerClient());

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    for (String name : dialog.getSelections()) {
                        requiredAttributes.add(name);
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e.getMessage());
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (requiredAttributesTable.getSelectionCount() == 0) return;

                for (TableItem item : requiredAttributesTable.getSelection()) {
                    String name = (String)item.getData();
                    requiredAttributes.remove(name);
                }

                refresh();
            }
        });

        Label optionalLabel = new Label(composite, SWT.NONE);
        optionalLabel.setText("Optional:");
        gd = new GridData();
        gd.horizontalSpan = 2;
        optionalLabel.setLayoutData(gd);

        optionalAttributesTable = new Table(composite, SWT.BORDER|SWT.FULL_SELECTION);
        optionalAttributesTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    SelectAttributeTypeDialog dialog = new SelectAttributeTypeDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add attributes...");

                    PenroseClient client = server.getClient();
                    dialog.setSchemaManagerClient(client.getSchemaManagerClient());

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    for (String name : dialog.getSelections()) {
                        optionalAttributes.add(name);
                    }

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e.getMessage());
                }
            }
        });

        removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (optionalAttributesTable.getSelectionCount() == 0) return;

                for (TableItem item : optionalAttributesTable.getSelection()) {
                    String name = (String)item.getData();
                    optionalAttributes.remove(name);
                }

                refresh();
            }
        });
	}

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
        requiredAttributesTable.removeAll();
        for (String name : requiredAttributes) {
            TableItem tableItem = new TableItem(requiredAttributesTable, SWT.NONE);
            tableItem.setText(name);
            tableItem.setData(name);
        }

        optionalAttributesTable.removeAll();
        for (String name : optionalAttributes) {
            TableItem tableItem = new TableItem(optionalAttributesTable, SWT.NONE);
            tableItem.setText(name);
            tableItem.setData(name);
        }
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Collection<String> getRequiredAttributes() {
        log.debug("Required attributes:");
        requiredAttributes.clear();

        for (TableItem item : requiredAttributesTable.getItems()) {
            String name = (String)item.getData();
            log.debug(" - "+name);
            requiredAttributes.add(name);
        }

        return requiredAttributes;
    }

    public void setRequiredAttributes(Collection<String> requiredAttributes) {
        if (this.requiredAttributes == requiredAttributes) return;
        this.requiredAttributes.clear();
        this.requiredAttributes.addAll(requiredAttributes);
    }

    public Collection<String> getOptionalAttributes() {
        log.debug("Optional attributes:");
        optionalAttributes.clear();

        for (TableItem item : optionalAttributesTable.getItems()) {
            String name = (String)item.getData();
            log.debug(" - "+name);
            optionalAttributes.add(name);
        }

        return optionalAttributes;
    }

    public void setOptionalAttributes(Collection<String> optionalAttributes) {
        if (this.optionalAttributes == optionalAttributes) return;
        this.optionalAttributes.clear();
        this.optionalAttributes.addAll(optionalAttributes);
    }
}