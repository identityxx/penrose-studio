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
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.management.partition.PartitionClient;
import org.safehaus.penrose.management.partition.PartitionManagerClient;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.studio.mapping.wizard.AttributeValueWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.ObjectClassWizardPage;
import org.safehaus.penrose.studio.mapping.wizard.StaticEntryRDNWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class StaticEntryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private String partitionName;
    private EntryConfig entryConfig;

    public StaticEntryRDNWizardPage rdnPage;
    public ObjectClassWizardPage ocPage;
    public AttributeValueWizardPage attrPage;

    public StaticEntryWizard(Project project, String partitionName, DN parentDn) {
        this.project = project;
        this.partitionName = partitionName;
        setWindowTitle("Adding static entry");

        rdnPage = new StaticEntryRDNWizardPage();
        rdnPage.setProject(project);
        rdnPage.setPartitionName(partitionName);
        rdnPage.setParentDn(parentDn);

        ocPage = new ObjectClassWizardPage(project);
        attrPage = new AttributeValueWizardPage(project, partitionName);
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
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean performFinish() {
        try {
            entryConfig = new EntryConfig();

            DNBuilder db = new DNBuilder();
            db.append(rdnPage.getRdn());
            db.append(rdnPage.getParentDn());
            entryConfig.setDn(db.toDn());

            entryConfig.setEntryClass(rdnPage.getClassName());
            entryConfig.addObjectClasses(ocPage.getSelectedObjectClasses());
            entryConfig.addAttributeMappings(attrPage.getAttributeMappings());
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
            ErrorDialog.open(e);
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
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
