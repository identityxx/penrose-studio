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

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.Server;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class ModuleMappingsWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    Server server;
    String partitionName;

    public ModuleMappingsWizardPage mappingsPage;

    ModuleConfig moduleConfig;

    public ModuleMappingsWizard() {
        setWindowTitle("Edit Module Mappings");
    }

    public void addPages() {
        try {
            mappingsPage = new ModuleMappingsWizardPage();

            mappingsPage.setServer(server);
            mappingsPage.setPartitionName(partitionName);
            mappingsPage.setModuleName(moduleConfig.getName());
            mappingsPage.setModuleMappings(moduleConfig.getModuleMappings());

            addPage(mappingsPage);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean canFinish() {
        if (!mappingsPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            log.debug("Mappings:");
            Collection<ModuleMapping> moduleMappings = mappingsPage.getModuleMappings();
            for (ModuleMapping moduleMapping : moduleMappings) {
                log.debug(" - "+moduleMapping.getBaseDn()+" "+moduleMapping.getScope()+" "+moduleMapping.getFilter());
                moduleMapping.setModuleName(moduleConfig.getName());
            }

            moduleConfig.setModuleMappings(moduleMappings);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
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

    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }

    public void setModuleConfig(ModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }
}