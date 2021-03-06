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
package org.safehaus.penrose.studio.module.editor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.module.ModuleClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.module.ModuleManagerClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.config.editor.ParametersPage;
import org.safehaus.penrose.studio.server.Server;

public class ModuleEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());

    Server server;
    String partitionName;
    String moduleName;

    ModuleClient moduleClient;
    ModuleConfig moduleConfig;

    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        ModuleEditorInput ei = (ModuleEditorInput)input;

        server = ei.getServer();
        partitionName = ei.getPartitionName();
        moduleName = ei.getModuleName();

        try {
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ModuleManagerClient moduleManagerClient = partitionClient.getModuleManagerClient();

            moduleClient = moduleManagerClient.getModuleClient(moduleName);
            moduleConfig = moduleClient.getModuleConfig();

        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }

        setPartName(ei.getName());
    }

    protected void addPages() {
        try {
            addPage(new ModulePropertiesPage(this));
            addPage(new ModuleMappingsPage(this));

            ParametersPage parametersPage = new ModuleParametersPage(this);
            parametersPage.setParameters(moduleConfig.getParameters());
            addPage(parametersPage);

            addPage(new ModuleMethodsPage(this, moduleClient));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
        try {
            store();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void doSaveAs() {
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setFocus() {
    }

    public void store() throws Exception {

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        
        ModuleManagerClient moduleManagerClient = partitionClient.getModuleManagerClient();
        moduleManagerClient.updateModule(moduleConfig.getName(), moduleConfig);

        partitionClient.store();

        setPartName(partitionName+"."+moduleConfig.getName());
    }

    public boolean isDirty() {
        return dirty;
    }
}
