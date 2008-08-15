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

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.directory.EntryAttributeConfig;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntryFieldConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.ldap.RDNBuilder;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.mapping.Relationship;
import org.safehaus.penrose.studio.mapping.wizard.AttributeValueWizardPage;
import org.safehaus.penrose.studio.directory.wizard.ObjectClassWizardPage;
import org.safehaus.penrose.studio.directory.wizard.RelationshipWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.source.wizard.SelectSourcesWizardPage;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class DynamicEntryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private String partitionName;
    private DN parentDn;
    private EntryConfig entryConfig = new EntryConfig();

    public SelectSourcesWizardPage sourcesPage;
    public RelationshipWizardPage relationshipPage;
    public ObjectClassWizardPage ocPage;
    public AttributeValueWizardPage attrPage;

    public DynamicEntryWizard() {
        setWindowTitle("Adding dynamic entry");
    }

    public void addPages() {

        sourcesPage = new SelectSourcesWizardPage();
        sourcesPage.setDescription("Add data sources. This step is optional.");
        sourcesPage.setProject(project);
        sourcesPage.setPartitionName(partitionName);

        relationshipPage = new RelationshipWizardPage(project, partitionName);

        ocPage = new ObjectClassWizardPage(project);
        //ocPage.setSelecteObjectClasses(entryConfig.getObjectClasses());

        attrPage = new AttributeValueWizardPage(project, partitionName);
        attrPage.setDefaultType(AttributeValueWizardPage.VARIABLE);

        addPage(sourcesPage);
        addPage(relationshipPage);
        addPage(ocPage);
        addPage(attrPage);
    }

    public boolean canFinish() {
        if (!sourcesPage.isPageComplete()) return false;
        if (!relationshipPage.isPageComplete()) return false;
        if (!ocPage.isPageComplete()) return false;
        if (!attrPage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (sourcesPage == page) {
            Collection<EntrySourceConfig> sourceMappings = sourcesPage.getSourceMappings();
            relationshipPage.setSourceMappings(sourceMappings);
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

            Collection<Relationship> relationships = relationshipPage.getRelationships();
            for (Relationship relationship : relationships) {

                String lfield = relationship.getLeftField();
                EntrySourceConfig lsource = entryConfig.getSourceConfig(relationship.getLeftSource());
                int lindex = entryConfig.getSourceConfigIndex(lsource);

                String rfield = relationship.getRightField();
                EntrySourceConfig rsource = entryConfig.getSourceConfig(relationship.getRightSource());
                int rindex = entryConfig.getSourceConfigIndex(rsource);

                if (lindex < rindex) { // rhs is dependent on lhs
                    rsource.addFieldConfig(new EntryFieldConfig(rfield, EntryFieldConfig.VARIABLE, relationship.getLhs()));
                } else {
                    lsource.addFieldConfig(new EntryFieldConfig(lfield, EntryFieldConfig.VARIABLE, relationship.getRhs()));
                }
            }

            entryConfig.addObjectClasses(ocPage.getSelectedObjectClasses());

            log.debug("Attribute mappings:");
            Collection<EntryAttributeConfig> attributeMappings = attrPage.getAttributeMappings();
            for (EntryAttributeConfig attributeMapping : attributeMappings) {
                log.debug(" - " + attributeMapping.getName() + " <= " + attributeMapping.getVariable());
                entryConfig.addAttributeConfig(attributeMapping);
            }

            RDNBuilder rb = new RDNBuilder();
            for (EntryAttributeConfig attributeMapping : attributeMappings) {
                if (!attributeMapping.isRdn()) continue;

                rb.set(attributeMapping.getName(), "...");
            }

            DNBuilder db = new DNBuilder();
            db.append(rb.toRdn());
            db.append(parentDn);
            entryConfig.setDn(db.toDn());

            log.debug("Reverse mappings:");
            for (EntryAttributeConfig attributeMapping : entryConfig.getAttributeConfigs()) {
                String name = attributeMapping.getName();

                String variable = attributeMapping.getVariable();
                if (variable == null) {
                    log.debug("Attribute " + name + " can't be reverse mapped.");
                    continue;
                }

                int j = variable.indexOf(".");
                String sourceName = variable.substring(0, j);
                String fieldName = variable.substring(j + 1);

                EntrySourceConfig sourceMapping = entryConfig.getSourceConfig(sourceName);
                Collection fieldMappings = sourceMapping.getFieldConfigs(fieldName);
                if (fieldMappings != null && !fieldMappings.isEmpty()) {
                    log.debug("Attribute " + name + " has been reverse mapped.");
                    continue;
                }

                log.debug(" - " + sourceName + "." + fieldName + " <= " + name);

                EntryFieldConfig fieldMapping = new EntryFieldConfig(fieldName, EntryFieldConfig.VARIABLE, attributeMapping.getName());
                sourceMapping.addFieldConfig(fieldMapping);
            }
/*
            DirectoryConfig directoryConfig = partitionConfig.getDirectoryConfig();
            directoryConfig.addEntryConfig(entryConfig);
            project.save(partitionConfig, directoryConfig);
*/
            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            partitionClient.createEntry(entryConfig);
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

    public DN getParentDn() {
        return parentDn;
    }

    public void setParentDn(DN parentDn) {
        this.parentDn = parentDn;
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
