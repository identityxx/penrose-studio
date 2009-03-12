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
package org.safehaus.penrose.studio.acl.wizard;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.jface.wizard.WizardPage;
import org.safehaus.penrose.acl.ACI;
import org.safehaus.penrose.studio.directory.dialog.ACIDialog;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Endi S. Dewata
 */
public class ACLWizardPage extends WizardPage {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "ACL";

    Table aclTable;

    List<ACI> acl = new ArrayList<ACI>();

    public ACLWizardPage() {
        super(NAME);
        setDescription("Enter the ACL of the entry.");
    }

    public void createControl(final Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

		composite.setLayout(new GridLayout());

        aclTable = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        aclTable.setHeaderVisible(true);
        aclTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(aclTable, SWT.NONE);
        tc.setText("Subject");
        tc.setWidth(100);

        tc = new TableColumn(aclTable, SWT.NONE);
        tc.setText("DN");
        tc.setWidth(100);

        tc = new TableColumn(aclTable, SWT.NONE);
        tc.setText("Target");
        tc.setWidth(100);

        tc = new TableColumn(aclTable, SWT.NONE);
        tc.setText("Attributes");
        tc.setWidth(50);

        tc = new TableColumn(aclTable, SWT.NONE);
        tc.setText("Scope");
        tc.setWidth(75);

        tc = new TableColumn(aclTable, SWT.NONE);
        tc.setText("Action");
        tc.setWidth(50);

        tc = new TableColumn(aclTable, SWT.NONE);
        tc.setText("Permission");
        tc.setWidth(75);

        aclTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        aclTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    if (aclTable.getSelectionCount() == 0) return;

                    TableItem item = aclTable.getSelection()[0];
                    ACI aci = (ACI)item.getData();

                    ACIDialog dialog = new ACIDialog(parent.getShell());
                    dialog.setText("Edit ACL...");

                    dialog.setSubject(aci.getSubject());
                    dialog.setDn(aci.getDn());
                    dialog.setTarget(aci.getTarget());
                    dialog.setAttributes(aci.getAttributes());
                    dialog.setScope(aci.getScope());
                    dialog.setAction(aci.getAction());
                    dialog.setPermission(aci.getPermission());

                    dialog.open();
                    if (!dialog.isSaved()) return;

                    aci.setSubject(dialog.getSubject());
                    aci.setDn(dialog.getDn());
                    aci.setTarget(dialog.getTarget());
                    aci.setAttributes(dialog.getAttributes());
                    aci.setScope(dialog.getScope());
                    aci.setAction(dialog.getAction());
                    aci.setPermission(dialog.getPermission());

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Composite buttons = new Composite(composite, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttons.setLayout(new RowLayout());

        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");

        //addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ACIDialog dialog = new ACIDialog(parent.getShell());
                    dialog.setText("Add ACL...");

                    dialog.open();
                    if (!dialog.isSaved()) return;

                    ACI aci = new ACI();
                    aci.setSubject(dialog.getSubject());
                    aci.setDn(dialog.getDn());
                    aci.setTarget(dialog.getTarget());
                    aci.setAttributes(dialog.getAttributes());
                    aci.setScope(dialog.getScope());
                    aci.setAction(dialog.getAction());
                    aci.setPermission(dialog.getPermission());

                    acl.add(aci);

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
		editButton.setText("Edit");

        //editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (aclTable.getSelectionCount() == 0) return;

                    TableItem item = aclTable.getSelection()[0];
                    ACI aci = (ACI)item.getData();

                    ACIDialog dialog = new ACIDialog(parent.getShell());
                    dialog.setText("Edit ACL...");

                    dialog.setSubject(aci.getSubject());
                    dialog.setDn(aci.getDn());
                    dialog.setTarget(aci.getTarget());
                    dialog.setAttributes(aci.getAttributes());
                    dialog.setScope(aci.getScope());
                    dialog.setAction(aci.getAction());
                    dialog.setPermission(aci.getPermission());

                    dialog.open();
                    if (!dialog.isSaved()) return;

                    aci.setSubject(dialog.getSubject());
                    aci.setDn(dialog.getDn());
                    aci.setTarget(dialog.getTarget());
                    aci.setAttributes(dialog.getAttributes());
                    aci.setScope(dialog.getScope());
                    aci.setAction(dialog.getAction());
                    aci.setPermission(dialog.getPermission());

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText("Remove");

        //removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                TableItem items[] = aclTable.getSelection();
                for (TableItem item : items) {
                    ACI aci = (ACI) item.getData();
                    acl.remove(aci);
                    item.dispose();
                }
            }
        });

        new Label(buttons, SWT.NONE);

        Button moveUpButton = new Button(buttons, SWT.PUSH);
		moveUpButton.setText("Move Up");

        //moveUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (aclTable.getSelectionCount() != 1) return;

                    int i = aclTable.getSelectionIndex();
                    if (i == 0) return;

                    ACI aci = acl.remove(i);
                    acl.add(i-1, aci);

                    refresh();
                    aclTable.setSelection(i-1);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button moveDownButton = new Button(buttons, SWT.PUSH);
		moveDownButton.setText("Move Down");

        //moveDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveDownButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (aclTable.getSelectionCount() != 1) return;

                    int i = aclTable.getSelectionIndex();
                    if (i >= acl.size()-1 ) return;

                    ACI aci = acl.remove(i);
                    acl.add(i+1, aci);

                    refresh();
                    aclTable.setSelection(i+1);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });
    }

    public Collection<ACI> getACL() {
        return acl;
    }

    public void setACL(Collection<ACI> acl) {
        this.acl.clear();
        for (ACI aci : acl) {
            addACI(aci);
        }
    }

    public void addACI(ACI aci) {
        try {
            acl.add((ACI)aci.clone());

        } catch (CloneNotSupportedException e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }

    public void refresh() {
        aclTable.removeAll();

        for (ACI aci : acl) {

            TableItem item = new TableItem(aclTable, SWT.NONE);
            item.setText(0, aci.getSubject());
            item.setText(1, aci.getDn() == null ? "" : aci.getDn().toString());
            item.setText(2, aci.getTarget());
            item.setText(3, aci.getAttributes() == null ? "" : aci.getAttributes());
            item.setText(4, aci.getScope());
            item.setText(5, aci.getAction());
            item.setText(6, aci.getPermission());
            item.setData(aci);
        }
    }
}