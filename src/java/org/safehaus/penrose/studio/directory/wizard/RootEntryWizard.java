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
import org.safehaus.penrose.acl.ACI;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.Server;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class RootEntryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private String partitionName;
    private EntryConfig entryConfig;

    public EntryDNWizardPage dnPage;
    public ObjectClassWizardPage ocPage;
    public AttributesWizardPage attributePage;

    public RootEntryWizard(Server server, String partitionName) {
        this.server = server;
        this.partitionName = partitionName;

        setWindowTitle("Adding root entry");
    }

    public boolean canFinish() {
        if (!dnPage.isPageComplete()) return false;
        if (!ocPage.isPageComplete()) return false;
        if (!attributePage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {

        dnPage = new EntryDNWizardPage();

        addPage(dnPage);

        ocPage = new ObjectClassWizardPage(server);

        addPage(ocPage);

        attributePage = new AttributesWizardPage();
        attributePage.setServer(server);
        attributePage.setPartitionName(partitionName);

        addPage(attributePage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        try {
            if (ocPage == page) {
                Collection<String> objectClasses = ocPage.getSelectedObjectClasses();
                attributePage.setObjectClasses(objectClasses);

                if (!objectClasses.isEmpty()) {
                    DN dn = new DN(dnPage.getDn());
                    RDN rdn = dn.getRdn();
                    attributePage.setRdn(rdn);
                }
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
            entryConfig.setDn(dnPage.getDn());
            //entryConfig.setEntryClass(dnPage.getClassName());
            entryConfig.addObjectClasses(ocPage.getSelectedObjectClasses());
            entryConfig.addAttributeConfigs(attributePage.getAttributeConfigs());

            entryConfig.addACI(new ACI("rs"));
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

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
