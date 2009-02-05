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
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class ModuleWizard extends Wizard {

    org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

    Server server;
    String partitionName;

    public ModulePropertiesWizardPage propertiesPage = new ModulePropertiesWizardPage();
    public ModuleParameterWizardPage parametersPage = new ModuleParameterWizardPage();
    public ModuleMappingWizardPage mappingsPage = new ModuleMappingWizardPage();

    public ModuleWizard() {
        setWindowTitle("New Module");
    }

    public void addPages() {
        addPage(propertiesPage);
        addPage(parametersPage);
        addPage(mappingsPage);
    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;
        if (!parametersPage.isPageComplete()) return false;
        if (!mappingsPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            ModuleConfig moduleConfig = new ModuleConfig();
            moduleConfig.setName(propertiesPage.getModuleName());
            moduleConfig.setModuleClass(propertiesPage.getModuleClass());

            Map<String,String> parameters = parametersPage.getParameters();
            for (String name : parameters.keySet()) {
                moduleConfig.setParameter(name, parameters.get(name));
            }
/*
            ModuleConfigManager moduleConfigManager = partitionConfig.getModuleConfigManager();
            moduleConfigManager.addModuleConfig(moduleConfig);

            Collection<ModuleMapping> mappings = mappingPage.getModuleMappings();
            for (ModuleMapping mapping : mappings) {
                mapping.setModuleName(propertyPage.getModuleName());
                moduleConfigManager.addModuleMapping(mapping);
            }

            project.save(partitionConfig, moduleConfigManager);
*/
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ModuleManagerClient moduleManagerCient = partitionClient.getModuleManagerClient();

            Collection<ModuleMapping> moduleMappings = mappingsPage.getModuleMappings();
            for (ModuleMapping moduleMapping : moduleMappings) {
                moduleMapping.setModuleName(propertiesPage.getModuleName());
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

    public boolean needsPreviousAndNextButtons() {
        return true;
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
}
