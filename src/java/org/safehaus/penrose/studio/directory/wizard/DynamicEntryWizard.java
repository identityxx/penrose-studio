/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.studio.source.wizard.SelectSourcesWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.RelationshipWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.ObjectClassWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.AttributeValueWizardPage;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class DynamicEntryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Partition partition;
    private EntryMapping parentMapping;
    private EntryMapping entryMapping = new EntryMapping();

    public SelectSourcesWizardPage sourcesPage;
    public RelationshipWizardPage relationshipPage;
    public ObjectClassWizardPage ocPage;
    public AttributeValueWizardPage attrPage;

    public DynamicEntryWizard(Partition partition, EntryMapping parentMapping) {
        this.partition = partition;
        this.parentMapping = parentMapping;
        setWindowTitle("Adding dynamic entry");

        sourcesPage = new SelectSourcesWizardPage(partition);
        sourcesPage.setDescription("Add data sources. This step is optional.");
        
        relationshipPage = new RelationshipWizardPage(partition);

        ocPage = new ObjectClassWizardPage();
        //ocPage.setSelecteObjectClasses(entryMapping.getObjectClasses());

        attrPage = new AttributeValueWizardPage(partition);
        attrPage.setDefaultType(AttributeValueWizardPage.VARIABLE);
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
            Collection sourceMappings = sourcesPage.getSourceMappings();
            relationshipPage.setSourceMappings(sourceMappings);
            attrPage.setSourceMappings(sourceMappings);

        } else if (ocPage == page) {
            Collection objectClasses = ocPage.getSelectedObjectClasses();
            attrPage.setObjectClasses(objectClasses);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            entryMapping.setParentDn(parentMapping.getDn());

            Collection sourceMappings = sourcesPage.getSourceMappings();
            for (Iterator i=sourceMappings.iterator(); i.hasNext(); ) {
                SourceMapping sourceMapping = (SourceMapping)i.next();
                entryMapping.addSourceMapping(sourceMapping);
            }

            Collection relationships = relationshipPage.getRelationships();
            for (Iterator i=relationships.iterator(); i.hasNext(); ) {
                Relationship relationship = (Relationship)i.next();
                entryMapping.addRelationship(relationship);
            }

            entryMapping.addObjectClasses(ocPage.getSelectedObjectClasses());

            Collection attributeMappings = attrPage.getAttributeMappings();
            entryMapping.addAttributeMappings(attributeMappings);

            StringBuffer sb = new StringBuffer();
            for (Iterator i=attributeMappings.iterator(); i.hasNext(); ) {
                AttributeMapping attributeMapping = (AttributeMapping)i.next();
                if (!"true".equals(attributeMapping.getRdn())) continue;

                if (sb.length() > 0) sb.append("+");

                sb.append(attributeMapping.getName());
                sb.append("=...");
            }

            entryMapping.setRdn(sb.toString());

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

            partition.addEntryMapping(entryMapping);

            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return false;
        }
    }

    public void addPages() {
        addPage(sourcesPage);
        addPage(relationshipPage);
        addPage(ocPage);
        addPage(attrPage);
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

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
