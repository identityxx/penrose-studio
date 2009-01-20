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
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.module.ModuleManagerClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.config.editor.ParametersPage;
import org.safehaus.penrose.studio.server.Server;

import java.util.Collection;

public class ModuleEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());

    Server server;
    String partitionName;
    String moduleName;

    ModuleClient moduleClient;
    ModuleConfig origModuleConfig;
    ModuleConfig moduleConfig;
    Collection<ModuleMapping> moduleMappings;

    boolean dirty;

    ParametersPage parametersPage;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        ModuleEditorInput ei = (ModuleEditorInput)input;

        server = ei.getProject();
        partitionName = ei.getPartitionName();
        moduleName = ei.getModuleName();

        try {
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ModuleManagerClient moduleManagerClient = partitionClient.getModuleManagerClient();

            moduleClient = moduleManagerClient.getModuleClient(moduleName);
            origModuleConfig = moduleClient.getModuleConfig();
            moduleMappings = moduleClient.getModuleMappings();

            moduleConfig = (ModuleConfig)origModuleConfig.clone();

        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }

        setPartName(ei.getName());
    }

    protected void addPages() {
        try {
            addPage(new ModulePropertiesPage(this));
            addPage(new ModuleMappingsPage(this));

            parametersPage = new ParametersPage(this, "Module Editor");
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
        moduleManagerClient.updateModule(origModuleConfig.getName(), moduleConfig);

        setPartName(partitionName+"."+moduleConfig.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();

        checkDirty();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void checkDirty() {
        try {
            dirty = false;
/*
            if (!origModuleConfig.equals(moduleConfig)) {
                dirty = true;
                return;
            }
*/
        } catch (Exception e) {
            log.error(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }
}
