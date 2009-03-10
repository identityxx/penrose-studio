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
package org.safehaus.penrose.studio.mapping.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.config.wizard.ParametersWizardPage;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.mapping.MappingConfig;
import org.safehaus.penrose.mapping.MappingManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @author Endi S. Dewata
 */
public class MappingWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    Server server;
    String partitionName;
    MappingConfig mappingConfig;

    public MappingPropertiesWizardPage propertiesPage;
    public MappingRulesWizardPage rulesPage;
    public MappingScriptWizardPage preScriptPage;
    public MappingScriptWizardPage postScriptPage;
    public ParametersWizardPage parametersPage;

    public MappingWizard() {
        setWindowTitle("New Mapping");
    }

    public void addPages() {

        propertiesPage = new MappingPropertiesWizardPage();
        propertiesPage.setMappingName(mappingConfig.getName());
        propertiesPage.setClassName(mappingConfig.getMappingClass());
        propertiesPage.setEnabled(mappingConfig.isEnabled());
        propertiesPage.setMappingDescription(mappingConfig.getDescription());

        addPage(propertiesPage);

        rulesPage = new MappingRulesWizardPage();
        rulesPage.setRuleConfigs(mappingConfig.getRuleConfigs());

        addPage(rulesPage);

        preScriptPage = new MappingScriptWizardPage();
        preScriptPage.setDescription("Enter mapping pre-script.");
        preScriptPage.setScript(mappingConfig.getPreScript());

        addPage(preScriptPage);

        postScriptPage = new MappingScriptWizardPage();
        postScriptPage.setDescription("Enter mapping post-script.");
        postScriptPage.setScript(mappingConfig.getPostScript());

        addPage(postScriptPage);

        parametersPage = new ParametersWizardPage();
        addPage(parametersPage);
    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;
        if (!rulesPage.isPageComplete()) return false;
        if (!preScriptPage.isPageComplete()) return false;
        if (!postScriptPage.isPageComplete()) return false;
        if (!parametersPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            mappingConfig.setName(propertiesPage.getMappingName());
            mappingConfig.setMappingClass(propertiesPage.getClassName());
            mappingConfig.setEnabled(propertiesPage.isEnabled());
            mappingConfig.setDescription(propertiesPage.getMappingDescription());

            mappingConfig.setRuleConfigs(rulesPage.getRuleConfigs());

            mappingConfig.setPreScript(preScriptPage.getScript());
            mappingConfig.setPostScript(postScriptPage.getScript());

            Map<String,String> parameters = parametersPage.getParameters();
            mappingConfig.setParameters(parameters);

            PartitionClient partitionClient = server.getClient().getPartitionManagerClient().getPartitionClient(partitionName);
            MappingManagerClient mappingManagerClient = partitionClient.getMappingManagerClient();

            mappingManagerClient.createMapping(mappingConfig);

            partitionClient.store();

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