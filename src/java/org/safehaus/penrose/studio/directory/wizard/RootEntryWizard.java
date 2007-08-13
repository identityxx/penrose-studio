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
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.studio.mapping.wizard.ObjectClassWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.AttributeValueWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.StaticEntryDNWizardPage;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.acl.ACI;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class RootEntryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private ProjectNode projectNode;
    private PartitionConfig partitionConfig;
    private EntryMapping entryMapping;

    public StaticEntryDNWizardPage dnPage;
    public ObjectClassWizardPage ocPage;
    public AttributeValueWizardPage attrPage;

    public RootEntryWizard(ProjectNode projectNode, PartitionConfig partition) {
        this.projectNode = projectNode;
        this.partitionConfig = partition;

        dnPage = new StaticEntryDNWizardPage(partition);
        ocPage = new ObjectClassWizardPage(projectNode);
        attrPage = new AttributeValueWizardPage(projectNode, partition);
        
        setWindowTitle("Adding root entry");
    }

    public boolean canFinish() {
        if (!dnPage.isPageComplete()) return false;
        if (!ocPage.isPageComplete()) return false;
        if (!attrPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        addPage(dnPage);
        addPage(ocPage);
        addPage(attrPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (ocPage == page) {
            Collection objectClasses = ocPage.getSelectedObjectClasses();
            attrPage.setObjectClasses(objectClasses);

            if (!objectClasses.isEmpty()) {
                DN dn = new DN(dnPage.getDn());
                RDN rdn = dn.getRdn();
                attrPage.setRdn(rdn);
            }
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            entryMapping = new EntryMapping();
            entryMapping.setDn(dnPage.getDn());
            entryMapping.addObjectClasses(ocPage.getSelectedObjectClasses());
            entryMapping.addAttributeMappings(attrPage.getAttributeMappings());

            entryMapping.addACI(new ACI("rs"));

            partitionConfig.getDirectoryConfigs().addEntryMapping(entryMapping);

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
}
