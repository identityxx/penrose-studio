/**
 * Copyright 2009 Red Hat, Inc.
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
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.acl.wizard.ACLWizardPage;
import org.safehaus.penrose.studio.attribute.wizard.AttributesWizardPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class RootEntryWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private String partitionName;

    public EntryDNWizardPage dnPage;
    public ObjectClassWizardPage objectClassesPage;
    public AttributesWizardPage attributesPage;
    public ACLWizardPage aclPage;

    public RootEntryWizard() {
        setWindowTitle("New Root Entry");
    }

    public void addPages() {

        dnPage = new EntryDNWizardPage();

        addPage(dnPage);

        objectClassesPage = new ObjectClassWizardPage();
        objectClassesPage.setServer(server);

        addPage(objectClassesPage);

        attributesPage = new AttributesWizardPage();
        attributesPage.setServer(server);
        attributesPage.setPartitionName(partitionName);

        addPage(attributesPage);

        aclPage = new ACLWizardPage();
        aclPage.addACI(new ACI("rs"));

        addPage(aclPage);
    }

    public boolean canFinish() {
        if (!dnPage.isPageComplete()) return false;
        if (!objectClassesPage.isPageComplete()) return false;
        if (!attributesPage.isPageComplete()) return false;
        if (!aclPage.isPageComplete()) return false;
        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        try {
            if (objectClassesPage == page) {
                Collection<String> objectClasses = objectClassesPage.getSelectedObjectClasses();
                attributesPage.setObjectClasses(objectClasses);

                if (!objectClasses.isEmpty()) {
                    DN dn = new DN(dnPage.getDn());
                    RDN rdn = dn.getRdn();
                    attributesPage.setRdn(rdn);
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
            EntryConfig entryConfig = new EntryConfig();
            entryConfig.setDn(dnPage.getDn());
            entryConfig.setObjectClasses(objectClassesPage.getSelectedObjectClasses());
            entryConfig.setAttributeConfigs(attributesPage.getAttributeConfigs());

            entryConfig.setACL(aclPage.getACL());

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
