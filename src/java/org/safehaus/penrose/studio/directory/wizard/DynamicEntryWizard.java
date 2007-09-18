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
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.studio.source.wizard.SelectSourcesWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.RelationshipWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.ObjectClassWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.AttributeValueWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.ldap.RDNBuilder;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.directory.DirectoryConfig;
import org.safehaus.penrose.directory.EntryMapping;
import org.safehaus.penrose.directory.AttributeMapping;
import org.safehaus.penrose.directory.FieldMapping;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class DynamicEntryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private PartitionConfig partitionConfig;
    private EntryMapping parentMapping;
    private EntryMapping entryMapping = new EntryMapping();

    public SelectSourcesWizardPage sourcesPage;
    public RelationshipWizardPage relationshipPage;
    public ObjectClassWizardPage ocPage;
    public AttributeValueWizardPage attrPage;

    public DynamicEntryWizard() {
        setWindowTitle("Adding dynamic entry");
    }

    public void addPages() {

        sourcesPage = new SelectSourcesWizardPage(partitionConfig);
        sourcesPage.setDescription("Add data sources. This step is optional.");

        relationshipPage = new RelationshipWizardPage(partitionConfig);

        ocPage = new ObjectClassWizardPage(project);
        //ocPage.setSelecteObjectClasses(entryMapping.getObjectClasses());

        attrPage = new AttributeValueWizardPage(project, partitionConfig);
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
            Collection<SourceMapping> sourceMappings = sourcesPage.getSourceMappings();
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
            Collection<SourceMapping> sourceMappings = sourcesPage.getSourceMappings();
            for (SourceMapping sourceMapping : sourceMappings) {
                entryMapping.addSourceMapping(sourceMapping);
            }

            Collection<Relationship> relationships = relationshipPage.getRelationships();
            for (Relationship relationship : relationships) {

                String lfield = relationship.getLeftField();
                SourceMapping lsource = entryMapping.getSourceMapping(relationship.getLeftSource());
                int lindex = entryMapping.getSourceMappingIndex(lsource);

                String rfield = relationship.getRightField();
                SourceMapping rsource = entryMapping.getSourceMapping(relationship.getRightSource());
                int rindex = entryMapping.getSourceMappingIndex(rsource);

                if (lindex < rindex) { // rhs is dependent on lhs
                    rsource.addFieldMapping(new FieldMapping(rfield, FieldMapping.VARIABLE, relationship.getLhs()));
                } else {
                    lsource.addFieldMapping(new FieldMapping(lfield, FieldMapping.VARIABLE, relationship.getRhs()));
                }
            }

            entryMapping.addObjectClasses(ocPage.getSelectedObjectClasses());

            log.debug("Attribute mappings:");
            Collection<AttributeMapping> attributeMappings = attrPage.getAttributeMappings();
            for (AttributeMapping attributeMapping : attributeMappings) {
                log.debug(" - " + attributeMapping.getName() + " <= " + attributeMapping.getVariable());
                entryMapping.addAttributeMapping(attributeMapping);
            }

            RDNBuilder rb = new RDNBuilder();
            for (AttributeMapping attributeMapping : attributeMappings) {
                if (!attributeMapping.isRdn()) continue;

                rb.set(attributeMapping.getName(), "...");
            }

            DNBuilder db = new DNBuilder();
            db.append(rb.toRdn());
            db.append(parentMapping.getDn());
            entryMapping.setDn(db.toDn());

            log.debug("Reverse mappings:");
            for (AttributeMapping attributeMapping : entryMapping.getAttributeMappings()) {
                String name = attributeMapping.getName();

                String variable = attributeMapping.getVariable();
                if (variable == null) {
                    log.debug("Attribute " + name + " can't be reverse mapped.");
                    continue;
                }

                int j = variable.indexOf(".");
                String sourceName = variable.substring(0, j);
                String fieldName = variable.substring(j + 1);

                SourceMapping sourceMapping = entryMapping.getSourceMapping(sourceName);
                Collection fieldMappings = sourceMapping.getFieldMappings(fieldName);
                if (fieldMappings != null && !fieldMappings.isEmpty()) {
                    log.debug("Attribute " + name + " has been reverse mapped.");
                    continue;
                }

                log.debug(" - " + sourceName + "." + fieldName + " <= " + name);

                FieldMapping fieldMapping = new FieldMapping(fieldName, FieldMapping.VARIABLE, attributeMapping.getName());
                sourceMapping.addFieldMapping(fieldMapping);
            }

            DirectoryConfig directoryConfig = partitionConfig.getDirectoryConfig();
            directoryConfig.addEntryMapping(entryMapping);
            project.save(partitionConfig, directoryConfig);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public EntryMapping getParentMapping() {
        return parentMapping;
    }

    public void setParentMapping(EntryMapping parentMapping) {
        this.parentMapping = parentMapping;
    }

    public EntryMapping getEntryMapping() {
        return entryMapping;
    }

    public void setEntryMapping(EntryMapping entryMapping) {
        this.entryMapping = entryMapping;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
