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

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;

/**
 * @author Endi S. Dewata
 */
public class ModuleMappingWizardPage extends WizardPage implements SelectionListener, TreeListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "LDAP Subtree";

    Tree baseDnTree;
    Text baseDnText;
    Text filterText;
    Combo scopeCombo;

    Server server;
    String partitionName;

    String baseDn;
    String filter;
    String scope;

    public ModuleMappingWizardPage() {
        super(NAME);
        setDescription("Select a subtree.");
    }

    public void init() throws Exception {
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        Label baseDnLabel = new Label(composite, SWT.NONE);
        baseDnLabel.setText("Base DN:");
        baseDnLabel.setLayoutData(new GridData(GridData.FILL));

        baseDnText = new Text(composite, SWT.BORDER);
        baseDnText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        baseDnText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                baseDn = baseDnText.getText().trim();
                baseDn = "".equals(baseDn) ? null : baseDn;

                setPageComplete(validatePage());
            }
        });

        baseDnTree = new Tree(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 100;
        gd.horizontalSpan = 2;
        baseDnTree.setLayoutData(gd);

        baseDnTree.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (baseDnTree.getSelectionCount() == 0) return;

                    TreeItem item = baseDnTree.getSelection()[0];
                    String entryName = (String)item.getData();

                    DirectoryClient directoryClient = getDirectoryClient();

                    DN dn = directoryClient.getEntryDn(entryName);
                    if (dn == null) return;

                    baseDnText.setText(dn.toString());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        baseDnTree.addTreeListener(this);

        Label filterLabel = new Label(composite, SWT.NONE);
        filterLabel.setText("Filter:");
        filterLabel.setLayoutData(new GridData(GridData.FILL));

        filterText = new Text(composite, SWT.BORDER);
        filterText.setText("(objectClass=*)");
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        filterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                filter = filterText.getText().trim();
                filter = "".equals(filter) ? null : filter;

                setPageComplete(validatePage());
            }
        });

        Label scopeLabel = new Label(composite, SWT.NONE);
        scopeLabel.setText("Scope:");
        scopeLabel.setLayoutData(new GridData(GridData.FILL));

        scopeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        scopeCombo.add("");
        scopeCombo.add("OBJECT");
        scopeCombo.add("ONELEVEL");
        scopeCombo.add("SUBTREE");
        scopeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        scopeCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                scope = scopeCombo.getText().trim();
                scope = "".equals(scope) ? null : scope;

                setPageComplete(validatePage());
            }
        });

        baseDnText.setText(baseDn == null ? "" : baseDn);
        filterText.setText(filter == null ? "" : filter);
        scopeCombo.setText(scope == null ? "" : scope);

        setPageComplete(validatePage());
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) refresh();
    }

    public void refresh() {
        try {
            baseDnTree.removeAll();

            DirectoryClient directoryClient = getDirectoryClient();

            for (String rootName : directoryClient.getRootEntryNames()) {
                DN dn = directoryClient.getEntryDn(rootName);

                TreeItem ti = new TreeItem(baseDnTree, SWT.NONE);
                ti.setText(dn.toString());
                ti.setData(rootName);

                new TreeItem(ti, SWT.NONE);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void treeCollapsed(TreeEvent event) {
    }

    public void treeExpanded(TreeEvent event) {
        try {
            if (event.item == null) return;

            TreeItem item = (TreeItem)event.item;
            expand(item);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public String getBaseDn() {
        return baseDn;
    }

    public String getFilter() {
        return filter;
    }

    public String getScope() {
        return scope;
    }

    public boolean validatePage() {
        return true;
    }

    public void widgetSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }

    public void widgetDefaultSelected(SelectionEvent event) {
        setPageComplete(validatePage());
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public DirectoryClient getDirectoryClient() throws Exception {
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        return partitionClient.getDirectoryClient();
    }

    public void expand(TreeItem item) throws Exception {

        for (TreeItem ti : item.getItems()) ti.dispose();

        DirectoryClient directoryClient = getDirectoryClient();

        try {
            String entryName = (String)item.getData();

            for (String childName : directoryClient.getChildNames(entryName)) {
                DN dn = directoryClient.getEntryDn(childName);
                TreeItem ti = new TreeItem(item, SWT.NONE);
                ti.setText(dn.getRdn().toString());
                ti.setData(childName);

                new TreeItem(ti, SWT.NONE);
            }

        } catch (Exception e) {
            TreeItem ti = new TreeItem(item, SWT.NONE);
            ti.setText("Error: "+e.getMessage());
        }
    }
}