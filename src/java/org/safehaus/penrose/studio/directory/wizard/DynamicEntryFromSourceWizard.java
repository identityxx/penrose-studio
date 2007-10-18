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
import org.safehaus.penrose.directory.EntryMapping;
import org.safehaus.penrose.directory.AttributeMapping;
import org.safehaus.penrose.directory.FieldMapping;
import org.safehaus.penrose.directory.SourceMapping;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.studio.mapping.wizard.AttributeValueWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.ObjectClassWizardPage;
import org.safehaus.penrose.studio.source.wizard.SelectSourcesWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.ldap.RDNBuilder;
import org.safehaus.penrose.directory.DirectoryConfig;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class DynamicEntryFromSourceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private PartitionConfig partitionConfig;
    private EntryMapping parentMapping;
    private EntryMapping entryMapping = new EntryMapping();

    public SelectSourcesWizardPage sourcesPage;
    public ObjectClassWizardPage ocPage;
    public AttributeValueWizardPage attrPage;

    public DynamicEntryFromSourceWizard(PartitionConfig partitionConfig, EntryMapping parentMapping) {
        this.partitionConfig = partitionConfig;
        this.parentMapping = parentMapping;
        setWindowTitle("Mapping Active Directory Users");
    }

    public void addPages() {

        sourcesPage = new SelectSourcesWizardPage(partitionConfig);
        sourcesPage.setDescription("Select a source.");

        ocPage = new ObjectClassWizardPage(project);
        //ocPage.setSelecteObjectClasses(entryMapping.getObjectClasses());

        attrPage = new AttributeValueWizardPage(project, partitionConfig);
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
            Collection sourceMappings = sourcesPage.getSourceMappings();
            attrPage.setSourceMappings(sourceMappings);

        } else if (ocPage == page) {
            Collection objectClasses = ocPage.getSelectedObjectClasses();
            attrPage.setObjectClasses(objectClasses);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            Collection sourceMappings = sourcesPage.getSourceMappings();
            for (Iterator i=sourceMappings.iterator(); i.hasNext(); ) {
                SourceMapping sourceMapping = (SourceMapping)i.next();
                entryMapping.addSourceMapping(sourceMapping);
            }

            entryMapping.addObjectClasses(ocPage.getSelectedObjectClasses());

            Collection attributeMappings = attrPage.getAttributeMappings();
            entryMapping.addAttributeMappings(attributeMappings);

            RDNBuilder rb = new RDNBuilder();
            for (Iterator i=attributeMappings.iterator(); i.hasNext(); ) {
                AttributeMapping attributeMapping = (AttributeMapping)i.next();
                if (!attributeMapping.isRdn()) continue;

                rb.set(attributeMapping.getName(), "...");
            }

            DNBuilder db = new DNBuilder();
            db.append(rb.toRdn());
            db.append(parentMapping.getDn());

            entryMapping.setDn(db.toDn());

            // add reverse mappings
            for (Iterator i=entryMapping.getAttributeMappings().iterator(); i.hasNext(); ) {
                AttributeMapping attributeMapping = (AttributeMapping)i.next();

                String variable = attributeMapping.getVariable();
                if (variable == null) continue;

                int j = variable.indexOf(".");
                String sourceName = variable.substring(0, j);
                String fieldName = variable.substring(j+1);

                FieldMapping fieldMapping = new FieldMapping(fieldName, FieldMapping.VARIABLE, attributeMapping.getName());

                SourceMapping sourceMapping = entryMapping.getSourceMapping(sourceName);
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
