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
package org.safehaus.penrose.studio.module.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.directory.DirectoryClient;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * @author Endi S. Dewata
 */
public class ModuleMappingsWizardPage extends WizardPage implements SelectionListener, ModifyListener {

    Logger log = Logger.getLogger(getClass());

    public final static String NAME = "Module Mappings";

    private Server server;
    private String partitionName;
    private String moduleName;

    Table mappingsTable;

    public Collection<ModuleMapping> moduleMappings = new LinkedHashSet<ModuleMapping>();

    public ModuleMappingsWizardPage() {
        super(NAME);
        setDescription("Enter module mappings.");
    }

    public void createControl(final Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

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
                    addModuleMapping();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button editButton = new Button(buttons, SWT.PUSH);
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        editButton.setText("Edit");

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    editModuleMapping();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        Button removeButton = new Button(buttons, SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        removeButton.setText("Remove");

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    removeModuleMapping();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        setPageComplete(validatePage());
    }

    public void addModuleMapping() throws Exception {

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        DirectoryClient directoryClient = partitionClient.getDirectoryClient();
        DN baseDn = directoryClient.getSuffix();

        ModuleMapping moduleMapping = new ModuleMapping();
        moduleMapping.setBaseDn(baseDn);
        moduleMapping.setScope("SUBTREE");
        moduleMapping.setFilter("(objectClass=*)");

        ModuleMappingWizard wizard = new ModuleMappingWizard();
        wizard.setWindowTitle("Add Module Mapping");
        wizard.setServer(server);
        wizard.setPartitionName(partitionName);
        wizard.setModuleMapping(moduleMapping);

        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.setPageSize(600, 300);

        int rc = dialog.open();
        if (rc == Window.CANCEL) return;

        moduleMappings.add(moduleMapping);

        refresh();
    }

    public void editModuleMapping() throws Exception {
        if (mappingsTable.getSelectionCount() == 0) return;

        TableItem item = mappingsTable.getSelection()[0];
        ModuleMapping moduleMapping = (ModuleMapping)item.getData();
        ModuleMapping newModuleMapping = (ModuleMapping)moduleMapping.clone();

        ModuleMappingWizard wizard = new ModuleMappingWizard();
        wizard.setWindowTitle("Edit Module Mapping");
        wizard.setServer(server);
        wizard.setPartitionName(partitionName);
        wizard.setModuleMapping(newModuleMapping);

        WizardDialog dialog = new WizardDialog(getShell(), wizard);
        dialog.setPageSize(600, 300);

        int rc = dialog.open();
        if (rc == Window.CANCEL) return;

        moduleMapping.copy(newModuleMapping);

        refresh();
    }

    public void removeModuleMapping() throws Exception {
        if (mappingsTable.getSelectionCount() == 0) return;

        TableItem item = mappingsTable.getSelection()[0];
        ModuleMapping moduleMapping = (ModuleMapping)item.getData();

        moduleMappings.remove(moduleMapping);

        refresh();
    }

    public Collection<ModuleMapping> getModuleMappings() {
        return moduleMappings;
    }

    public void setModuleMappings(Collection<ModuleMapping> moduleMappings) throws Exception {
        if (this.moduleMappings == moduleMappings) return;
        this.moduleMappings.clear();
        if (moduleMappings == null) return;

        for (ModuleMapping moduleMapping : moduleMappings) {
            ModuleMapping mm = (ModuleMapping)moduleMapping.clone();
            mm.setModuleName(null);
            this.moduleMappings.add(mm);
        }
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

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) refresh();
    }
    
    public void refresh() {
        mappingsTable.removeAll();

        for (ModuleMapping moduleMapping : moduleMappings) {
            TableItem item = new TableItem(mappingsTable, SWT.NONE);

            DN baseDn = moduleMapping.getBaseDn();
            item.setText(0, baseDn == null ? "" : baseDn.toString());

            String scope = moduleMapping.getScope();
            item.setText(1, scope == null ? "" : scope);

            String filter = moduleMapping.getFilter();
            item.setText(2, filter == null ? "" : filter);

            item.setData(moduleMapping);
        }
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
}
