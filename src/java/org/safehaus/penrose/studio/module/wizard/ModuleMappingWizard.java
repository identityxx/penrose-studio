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

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.module.ModuleManagerClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class ModuleMappingWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    Server project;
    String partitionName;

    public ModuleMappingWizardPage mappingPage = new ModuleMappingWizardPage();

    ModuleConfig moduleConfig;

    public ModuleMappingWizard() {
        setWindowTitle("Edit Module Mappings");
    }

    public boolean canFinish() {
        if (!mappingPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {

            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ModuleManagerClient moduleManagerCient = partitionClient.getModuleManagerClient();

            Collection<ModuleMapping> moduleMappings = mappingPage.getModuleMappings();
            for (ModuleMapping moduleMapping : moduleMappings) {
                moduleMapping.setModuleName(moduleConfig.getName());
            }

            moduleManagerCient.createModule(moduleConfig, moduleMappings);

            partitionClient.store();

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public void addPages() {
        addPage(mappingPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public Server getProject() {
        return project;
    }

    public void setProject(Server project) {
        this.project = project;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }

    public void setModuleConfig(ModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }
}