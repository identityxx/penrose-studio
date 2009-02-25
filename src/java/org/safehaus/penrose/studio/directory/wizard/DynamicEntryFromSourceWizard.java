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
package org.safehaus.penrose.studio.directory.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.studio.directory.wizard.EntrySourceWizardPage;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.attribute.wizard.AttributesWizardPage;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.ldap.RDNBuilder;
import org.safehaus.penrose.directory.*;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class DynamicEntryFromSourceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private String partitionName;
    private EntryConfig parentConfig;
    private EntryConfig entryConfig = new EntryConfig();

    public EntrySourceWizardPage sourcesPage;
    public ObjectClassWizardPage ocPage;
    public AttributesWizardPage attributePage;

    public DynamicEntryFromSourceWizard(String partitionName, EntryConfig parentConfig) {
        this.partitionName = partitionName;
        this.parentConfig = parentConfig;
        setWindowTitle("Mapping Active Directory Users");
    }

    public void addPages() {

        sourcesPage = new EntrySourceWizardPage();
        sourcesPage.setDescription("Select a source.");
        sourcesPage.setServer(server);
        sourcesPage.setPartitionName(partitionName);

        addPage(sourcesPage);

        ocPage = new ObjectClassWizardPage();
        ocPage.setServer(server);
        //ocPage.setSelecteObjectClasses(entryConfig.getObjectClasses());

        addPage(ocPage);

        attributePage = new AttributesWizardPage();
        attributePage.setServer(server);
        attributePage.setPartitionName(partitionName);
        attributePage.setDefaultType(AttributesWizardPage.VARIABLE);

        addPage(attributePage);
    }

    public boolean canFinish() {
        if (!sourcesPage.isPageComplete()) return false;
        if (!ocPage.isPageComplete()) return false;
        if (!attributePage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (sourcesPage == page) {
            Collection<EntrySourceConfig> sourceMappings = sourcesPage.getEntrySourceConfigs();
            attributePage.setEntrySourceConfigs(sourceMappings);

        } else if (ocPage == page) {
            Collection<String> objectClasses = ocPage.getSelectedObjectClasses();
            attributePage.setObjectClasses(objectClasses);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            Collection<EntrySourceConfig> sourceMappings = sourcesPage.getEntrySourceConfigs();
            for (EntrySourceConfig sourceMapping : sourceMappings) {
                entryConfig.addSourceConfig(sourceMapping);
            }

            entryConfig.addObjectClasses(ocPage.getSelectedObjectClasses());

            Collection<EntryAttributeConfig> attributeMappings = attributePage.getAttributeConfigs();
            entryConfig.addAttributeConfigs(attributeMappings);

            RDNBuilder rb = new RDNBuilder();
            for (EntryAttributeConfig attributeMapping : attributeMappings) {
                if (!attributeMapping.isRdn()) continue;

                rb.set(attributeMapping.getName(), "...");
            }

            DNBuilder db = new DNBuilder();
            db.append(rb.toRdn());
            db.append(parentConfig.getDn());

            entryConfig.setDn(db.toDn());

            // add reverse mappings
            for (EntryAttributeConfig attributeConfig : entryConfig.getAttributeConfigs()) {

                String variable = attributeConfig.getVariable();
                if (variable == null) continue;

                int j = variable.indexOf(".");
                String sourceName = variable.substring(0, j);
                String fieldName = variable.substring(j + 1);

                EntryFieldConfig fieldConfig = new EntryFieldConfig(fieldName, EntryFieldConfig.VARIABLE, attributeConfig.getName());

                EntrySourceConfig sourceConfig = entryConfig.getSourceConfig(sourceName);
                sourceConfig.addFieldConfig(fieldConfig);
            }
/*
            DirectoryConfig directoryConfig = partitionConfig.getDirectoryConfig();
            directoryConfig.addEntryConfig(entryConfig);
            project.save(partitionConfig, directoryConfig);
*/
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

            DirectoryClient directoryClient = partitionClient.getDirectoryClient();
            directoryClient.createEntry(entryConfig);

            partitionClient.store();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public EntryConfig getParentConfig() {
        return parentConfig;
    }

    public void setParentConfig(EntryConfig parentConfig) {
        this.parentConfig = parentConfig;
    }

    public EntryConfig getEntryConfig() {
        return entryConfig;
    }

    public void setEntryConfig(EntryConfig entryConfig) {
        this.entryConfig = entryConfig;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
