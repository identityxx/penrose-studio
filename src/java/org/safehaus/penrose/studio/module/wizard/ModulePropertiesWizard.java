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
import org.safehaus.penrose.module.ModuleManagerClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class ModulePropertiesWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    ModuleConfig moduleConfig;

    public ModulePropertiesWizardPage propertiesPage = new ModulePropertiesWizardPage();

    public ModulePropertiesWizard() {
        setWindowTitle("Edit Module Properties");
    }

    public void addPages() {
        propertiesPage = new ModulePropertiesWizardPage();

        propertiesPage.setModuleName(moduleConfig.getName());
        propertiesPage.setClassName(moduleConfig.getModuleClass());
        propertiesPage.setEnabled(moduleConfig.isEnabled());
        propertiesPage.setModuleDescription(moduleConfig.getDescription());

        addPage(propertiesPage);
    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            moduleConfig.setName(propertiesPage.getModuleName());
            moduleConfig.setModuleClass(propertiesPage.getClassName());
            moduleConfig.setEnabled(propertiesPage.isEnabled());
            moduleConfig.setDescription(propertiesPage.getModuleDescription());

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

    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }

    public void setModuleConfig(ModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }
}