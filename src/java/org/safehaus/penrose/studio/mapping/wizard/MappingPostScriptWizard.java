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
package org.safehaus.penrose.studio.mapping.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.mapping.MappingConfig;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class MappingPostScriptWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    Server server;
    String partitionName;
    MappingConfig mappingConfig;

    public MappingScriptWizardPage scriptPage;

    public MappingPostScriptWizard() {
        setWindowTitle("Edit Mapping Post-Script");
    }

    public void addPages() {

        scriptPage = new MappingScriptWizardPage();
        scriptPage.setDescription("Enter mapping post-script.");
        scriptPage.setScript(mappingConfig.getPostScript());

        addPage(scriptPage);
    }

    public boolean canFinish() {
        if (!scriptPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            mappingConfig.setPostScript(scriptPage.getScript());

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

    public MappingConfig getMappingConfig() {
        return mappingConfig;
    }

    public void setMappingConfig(MappingConfig mappingConfig) {
        this.mappingConfig = mappingConfig;
    }
}