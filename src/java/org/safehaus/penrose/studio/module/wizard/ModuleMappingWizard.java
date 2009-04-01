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
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.ldap.DN;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class ModuleMappingWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    Server server;
    String partitionName;
    ModuleMapping moduleMapping;

    ModuleMappingWizardPage mappingPage;

    public ModuleMappingWizard() {
    }

    public void addPages() {
        try {
            mappingPage = new ModuleMappingWizardPage();

            mappingPage.setServer(server);
            mappingPage.setPartitionName(partitionName);

            DN baseDn = moduleMapping.getBaseDn();
            mappingPage.setBaseDn(baseDn == null ? null : baseDn.toString());

            mappingPage.setFilter(moduleMapping.getFilter());
            mappingPage.setScope(moduleMapping.getScope());

            addPage(mappingPage);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean canFinish() {
        if (!mappingPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            moduleMapping.setBaseDn(mappingPage.getBaseDn());
            moduleMapping.setFilter(mappingPage.getFilter());
            moduleMapping.setScope(mappingPage.getScope());

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

    public ModuleMapping getModuleMapping() {
        return moduleMapping;
    }

    public void setModuleMapping(ModuleMapping moduleMapping) {
        this.moduleMapping = moduleMapping;
    }
}