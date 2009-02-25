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
package org.safehaus.penrose.studio.rootDse.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.acl.wizard.ACLWizardPage;
import org.safehaus.penrose.studio.attribute.wizard.AttributesWizardPage;
import org.safehaus.penrose.acl.ACI;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class DefaultRootDSEWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public AttributesWizardPage attributesPage;
    public ACLWizardPage aclPage;

    private Server server;
    private String partitionName;

    EntryConfig entryConfig;

    public DefaultRootDSEWizard() {
        setWindowTitle("New Default Root DSE");
    }

    public void init() throws Exception {

        entryConfig = new EntryConfig();
        entryConfig.setEntryClass("org.safehaus.penrose.directory.RootEntry");

        entryConfig.addObjectClass("extensibleObject");

        PenroseClient penroseClient = server.getClient();

        try {
            String vendorName = penroseClient.getProductVendor();
            entryConfig.addAttributeConfig("vendorName", vendorName);

            String vendorVersion = penroseClient.getProductName()+" "+penroseClient.getProductVersion();
            entryConfig.addAttributeConfig("vendorVersion", vendorVersion);
            
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
        entryConfig.addAttributeConfig("supportedLDAPVersion", "3");
        entryConfig.addAttributeConfig("subschemaSubentry", "cn=Subschema");
        entryConfig.addAttributeConfig("changelog", "cn=changelog");
        entryConfig.addAttributeConfig("namingContexts", "dc=Example,dc=com");
    }

    public void addPages() {
        try {
            attributesPage = new AttributesWizardPage();
            attributesPage.setServer(server);
            attributesPage.setPartitionName(partitionName);
            attributesPage.setAttributeConfigs(entryConfig.getAttributeConfigs());

            addPage(attributesPage);

            aclPage = new ACLWizardPage();
            aclPage.addACI(new ACI("rs"));

            addPage(aclPage);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public boolean canFinish() {
        if (!attributesPage.isPageComplete()) return false;
        if (!aclPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            entryConfig.addAttributeConfigs(attributesPage.getAttributeConfigs());

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
            return false;
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }
}