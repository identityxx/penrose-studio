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
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.DNBuilder;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.mapping.wizard.AttributeValueWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.source.wizard.SelectSourcesWizardPage;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class EntryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private String partitionName;
    private DN parentDn;

    private EntryConfig entryConfig;

    public EntryRDNWizardPage rdnPage;
    public ObjectClassWizardPage ocPage;
    public AttributeValueWizardPage attrPage;
    public SelectSourcesWizardPage sourcesPage;

    public EntryWizard(Project project, String partitionName, DN parentDn) {
        this.project = project;
        this.partitionName = partitionName;
        this.parentDn = parentDn;

        setWindowTitle("Adding entry");
    }

    public void addPages() {

        rdnPage = new EntryRDNWizardPage();
        rdnPage.setProject(project);
        rdnPage.setPartitionName(partitionName);
        rdnPage.setParentDn(parentDn);

        addPage(rdnPage);

        ocPage = new ObjectClassWizardPage(project);

        addPage(ocPage);

        attrPage = new AttributeValueWizardPage(project, partitionName);

        addPage(attrPage);

        sourcesPage = new SelectSourcesWizardPage();
        sourcesPage.setDescription("Add data sources.");
        sourcesPage.setProject(project);
        sourcesPage.setPartitionName(partitionName);

        addPage(sourcesPage);
    }

    public boolean canFinish() {
        if (!rdnPage.isPageComplete()) return false;
        if (!ocPage.isPageComplete()) return false;
        if (!attrPage.isPageComplete()) return false;
        if (!sourcesPage.isPageComplete()) return false;
        return true;
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
            entryConfig.addAttributeConfigs(attrPage.getAttributeMappings());
            entryConfig.addSourceConfigs(sourcesPage.getSourceMappings());

            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

            DirectoryClient directoryClient = partitionClient.getDirectoryClient();
            directoryClient.createEntry(entryConfig);

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
