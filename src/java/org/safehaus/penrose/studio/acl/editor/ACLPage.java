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
package org.safehaus.penrose.studio.acl.editor;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.acl.ACI;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntryClient;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.directory.editor.EntryEditor;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.acl.wizard.ACLWizard;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class ACLPage extends FormPage implements ModifyListener {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table aclTable;
    Table inheritedAclTable;

    EntryEditor editor;

    Server server;
    String partitionName;
    EntryConfig entryConfig;

    public ACLPage(EntryEditor editor, Server server, String partitionName, EntryConfig entryConfig) {
        super(editor, "ACL", "  ACL  ");

        this.editor = editor;

        this.server = server;
        this.partitionName = partitionName;
        this.entryConfig   = entryConfig;
    }

    public void createFormContent(IManagedForm managedForm) {
        try {
            toolkit = managedForm.getToolkit();

            ScrolledForm form = managedForm.getForm();
            form.setText("Entry Editor");

            Composite body = form.getBody();
            body.setLayout(new GridLayout());

            Section aclSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            aclSection.setText("Access Control List");
            aclSection.setLayoutData(new GridData(GridData.FILL_BOTH));

            Composite aclControl = createACLControl(aclSection);
            aclSection.setClient(aclControl);

            Section inheritedACLSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            inheritedACLSection.setText("Inherited Access Control List");
            inheritedACLSection.setLayoutData(new GridData(GridData.FILL_BOTH));

            Composite inheritedACLControl = createInheritedACLControl(inheritedACLSection);
            inheritedACLSection.setClient(inheritedACLControl);

            refresh();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
	}

    public Composite createACLControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createACLLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createACLRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createACLLeftControl(final Composite parent) {

        aclTable = toolkit.createTable(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
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

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        aclTable.setLayoutData(gd);
        aclTable.setLayout(new FillLayout());
/*
        aclTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    if (aclTable.getSelectionCount() == 0) return;

                    TableItem item = aclTable.getSelection()[0];
                    ACI aci = (ACI)item.getData();

                    ACIDialog dialog = new ACIDialog(getSite().getShell());
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
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
*/
        return aclTable;
    }

    public Composite createACLRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
/*
        Button addButton = new Button(buttons, SWT.PUSH);
        addButton.setText("Add");

        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ACIDialog dialog = new ACIDialog(getSite().getShell());
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

                    entryConfig.addACI(aci);

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
*/
        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");

        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ACLWizard wizard = new ACLWizard();
                    wizard.setEntryConfig(entryConfig);

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    editor.store();

                    refresh();
/*
                    if (aclTable.getSelectionCount() == 0) return;

                    TableItem item = aclTable.getSelection()[0];
                    ACI aci = (ACI)item.getData();

                    ACIDialog dialog = new ACIDialog(getSite().getShell());
                    dialog.setText("Edit ACL...");

                    dialog.setSubject(aci.getSubject());
                    dialog.setDn(aci.getDn().toString());
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
                    checkDirty();
*/
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
/*
        Button removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText("Remove");

        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                TableItem items[] = aclTable.getSelection();
                for (TableItem item : items) {
                    ACI aci = (ACI) item.getData();
                    entryConfig.removeACI(aci);
                    item.dispose();
                }
                checkDirty();
            }
        });

        new Label(buttons, SWT.NONE);

        Button moveUpButton = new Button(buttons, SWT.PUSH);
		moveUpButton.setText("Move Up");

        moveUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveUpButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (aclTable.getSelectionCount() == 0) return;

                    TableItem items[] = aclTable.getItems();
                    ACI acl[] = new ACI[items.length];

                    int index = aclTable.getSelectionIndex();
                    if (index == 0) return;

                    for (int i=0; i<items.length; i++) {
                        TableItem item = items[i];
                        acl[i] = (ACI)item.getData();
                    }

                    ACI temp = acl[index];
                    acl[index] = acl[index-1];
                    acl[index-1] = temp;

                    entryConfig.removeACL();
                    for (ACI aci : acl) {
                        entryConfig.addACI(aci);
                    }

                    refresh();
                    aclTable.setSelection(index-1);

                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Button moveDownButton = new Button(buttons, SWT.PUSH);
		moveDownButton.setText("Move Down");

        moveDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        moveDownButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (aclTable.getSelectionCount() == 0) return;

                    TableItem items[] = aclTable.getItems();
                    ACI acl[] = new ACI[items.length];

                    int index = aclTable.getSelectionIndex();
                    if (index == items.length-1) return;

                    for (int i=0; i<items.length; i++) {
                        TableItem item = items[i];
                        acl[i] = (ACI)item.getData();
                    }

                    ACI temp = acl[index];
                    acl[index] = acl[index+1];
                    acl[index+1] = temp;

                    entryConfig.removeACL();
                    for (ACI aci : acl) {
                        entryConfig.addACI(aci);
                    }

                    refresh();
                    aclTable.setSelection(index+1);

                    checkDirty();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
*/
        return composite;
    }

    public void refresh() throws Exception {
        aclTable.removeAll();

        Collection<ACI> acl = entryConfig.getACL();
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

        inheritedAclTable.removeAll();

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        DirectoryClient directoryClient = partitionClient.getDirectoryClient();

        EntryClient entryClient = directoryClient.getEntryClient(entryConfig.getName());
        String parentId = entryClient.getParentName();
        log.debug("Parent ID: "+parentId);

        if (parentId == null) return;

        EntryClient parentClient = directoryClient.getEntryClient(parentId);
        EntryConfig parentConfig = parentClient.getEntryConfig();

        //PartitionConfig partitionConfig = editor.getPartitionConfig();
        //EntryConfig parentConfig = partitionConfig.getDirectoryConfig().getParent(entryConfig);

        while (parentConfig != null) {

            log.debug("Showing ACL from "+parentConfig.getDn());

            Collection<ACI> list = parentConfig.getACL();
            for (ACI aci : list) {
                if (ACI.SCOPE_OBJECT.equals(aci.getScope())) continue;

                TableItem item = new TableItem(inheritedAclTable, SWT.NONE);
                item.setText(0, aci.getSubject());
                item.setText(1, aci.getDn() == null ? "" : aci.getDn().toString());
                item.setText(2, aci.getTarget());
                item.setText(3, aci.getAttributes() == null ? "" : aci.getAttributes());
                item.setText(4, aci.getScope());
                item.setText(5, aci.getAction());
                item.setText(6, aci.getPermission());
                item.setText(7, parentConfig.getDn().toString());
                item.setData(aci);
            }

            parentId = parentClient.getParentName();
            log.debug("Parent ID: "+parentId);
            if (parentId == null) break;

            parentClient = directoryClient.getEntryClient(parentId);
            parentConfig = parentClient.getEntryConfig();

            //parentConfig = partitionConfig.getDirectoryConfig().getParent(parentConfig);
        }
    }

    public Composite createInheritedACLControl(Composite parent) throws Exception {

        inheritedAclTable = toolkit.createTable(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        inheritedAclTable.setHeaderVisible(true);
        inheritedAclTable.setLinesVisible(true);

        //GridData gd = new GridData(GridData.FILL_BOTH);
        //gd.heightHint = 100;
        //inheritedAclTable.setLayoutData(gd);
        //inheritedAclTable.setLayout(new FillLayout());

        TableColumn tc = new TableColumn(inheritedAclTable, SWT.NONE);
        tc.setText("Subject");
        tc.setWidth(100);

        tc = new TableColumn(inheritedAclTable, SWT.NONE);
        tc.setText("DN");
        tc.setWidth(100);

        tc = new TableColumn(inheritedAclTable, SWT.NONE);
        tc.setText("Target");
        tc.setWidth(100);

        tc = new TableColumn(inheritedAclTable, SWT.NONE);
        tc.setText("Attributes");
        tc.setWidth(50);

        tc = new TableColumn(inheritedAclTable, SWT.NONE);
        tc.setText("Scope");
        tc.setWidth(75);

        tc = new TableColumn(inheritedAclTable, SWT.NONE);
        tc.setText("Action");
        tc.setWidth(50);

        tc = new TableColumn(inheritedAclTable, SWT.NONE);
        tc.setText("Permission");
        tc.setWidth(75);

        tc = new TableColumn(inheritedAclTable, SWT.NONE);
        tc.setText("Source");
        tc.setWidth(120);

        return inheritedAclTable;
    }

    public void modifyText(ModifyEvent event) {
        checkDirty();
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}
