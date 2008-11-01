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
import org.safehaus.penrose.studio.mapping.wizard.AttributeValueWizardPage;
import org.safehaus.penrose.studio.directory.wizard.ObjectClassWizardPage;
import org.safehaus.penrose.studio.source.wizard.SelectSourcesWizardPage;
import org.safehaus.penrose.studio.project.Project;
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

    private Project project;
    private String partitionName;
    private EntryConfig parentConfig;
    private EntryConfig entryConfig = new EntryConfig();

    public SelectSourcesWizardPage sourcesPage;
    public ObjectClassWizardPage ocPage;
    public AttributeValueWizardPage attrPage;

    public DynamicEntryFromSourceWizard(String partitionName, EntryConfig parentConfig) {
        this.partitionName = partitionName;
        this.parentConfig = parentConfig;
        setWindowTitle("Mapping Active Directory Users");
    }

    public void addPages() {

        sourcesPage = new SelectSourcesWizardPage();
        sourcesPage.setDescription("Select a source.");
        sourcesPage.setProject(project);
        sourcesPage.setPartitionName(partitionName);

        ocPage = new ObjectClassWizardPage(project);
        //ocPage.setSelecteObjectClasses(entryConfig.getObjectClasses());

        attrPage = new AttributeValueWizardPage(project, partitionName);
        attrPage.setDefaultType(AttributeValueWizardPage.VARIABLE);

        addPage(sourcesPage);
        addPage(ocPage);
        addPage(attrPage);
    }

    public boolean canFinish() {
        if (!sourcesPage.isPageComplete()) return false;
        if (!ocPage.isPageComplete()) return false;
        if (!attrPage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (sourcesPage == page) {
            Collection<EntrySourceConfig> sourceMappings = sourcesPage.getSourceMappings();
            attrPage.setSourceMappings(sourceMappings);

        } else if (ocPage == page) {
            Collection<String> objectClasses = ocPage.getSelectedObjectClasses();
            attrPage.setObjectClasses(objectClasses);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            Collection<EntrySourceConfig> sourceMappings = sourcesPage.getSourceMappings();
            for (EntrySourceConfig sourceMapping : sourceMappings) {
                entryConfig.addSourceConfig(sourceMapping);
            }

            entryConfig.addObjectClasses(ocPage.getSelectedObjectClasses());

            Collection<EntryAttributeConfig> attributeMappings = attrPage.getAttributeMappings();
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
            PenroseClient client = project.getClient();
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
