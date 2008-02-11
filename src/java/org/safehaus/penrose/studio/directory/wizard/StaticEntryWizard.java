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
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.studio.mapping.wizard.ObjectClassWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.AttributeValueWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.StaticEntryRDNWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.directory.DirectoryConfig;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class StaticEntryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private PartitionConfig partitionConfig;
    private EntryMapping parentMapping;
    private EntryMapping entryMapping;

    public StaticEntryRDNWizardPage rdnPage;
    public ObjectClassWizardPage ocPage;
    public AttributeValueWizardPage attrPage;

    public StaticEntryWizard(Project project, PartitionConfig partitionConfig, EntryMapping parentMapping) {
        this.project = project;
        this.partitionConfig = partitionConfig;
        this.parentMapping = parentMapping;
        setWindowTitle("Adding static entry");

        rdnPage = new StaticEntryRDNWizardPage(partitionConfig, parentMapping);
        ocPage = new ObjectClassWizardPage(project);
        attrPage = new AttributeValueWizardPage(project, partitionConfig);
    }

    public boolean canFinish() {
        if (!rdnPage.isPageComplete()) return false;
        if (!ocPage.isPageComplete()) return false;
        if (!attrPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        addPage(rdnPage);
        addPage(ocPage);
        addPage(attrPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
            try {
            if (rdnPage == page) {
                RDN rdn = new RDN(rdnPage.getRdn());
                attrPage.setRdn(rdn);

            } else if (ocPage == page) {
                Collection objectClasses = ocPage.getSelectedObjectClasses();
                attrPage.setObjectClasses(objectClasses);
            }

            return super.getNextPage(page);
                
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean performFinish() {
        try {
            entryMapping = new EntryMapping();

            DNBuilder db = new DNBuilder();
            db.append(rdnPage.getRdn());
            db.append(rdnPage.getParentDn());
            entryMapping.setDn(db.toDn());

            entryMapping.addObjectClasses(ocPage.getSelectedObjectClasses());
            entryMapping.addAttributeMappings(attrPage.getAttributeMappings());

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

    public EntryMapping getParentMapping() {
        return parentMapping;
    }

    public void setParentMapping(EntryMapping parentMapping) {
        this.parentMapping = parentMapping;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
