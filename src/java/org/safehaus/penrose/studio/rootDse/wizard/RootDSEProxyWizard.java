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
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.acl.ACI;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class RootDSEProxyWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public SelectConnectionWizardPage connectionPage;
    public ACLWizardPage aclPage;

    private Server server;
    private String partitionName;

    public RootDSEProxyWizard() {
        setWindowTitle("New Root DSE Proxy");
    }

    public void addPages() {

        connectionPage = new SelectConnectionWizardPage();
        connectionPage.setDescription("Select an LDAP connection.");

        connectionPage.setServer(server);
        connectionPage.setPartitionName(partitionName);
        connectionPage.setAdapterType("LDAP");

        addPage(connectionPage);

        aclPage = new ACLWizardPage();
        aclPage.addACI(new ACI("rs"));

        addPage(aclPage);
    }

    public boolean canFinish() {
        return connectionPage.isPageComplete();
    }

    public boolean performFinish() {
        try {
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Source
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            SourceConfig sourceConfig = new SourceConfig();
            sourceConfig.setName(connectionPage.getConnectionName()+"_root_dse");
            sourceConfig.setConnectionName(connectionPage.getConnectionName());

            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
            sourceManagerClient.createSource(sourceConfig);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Entry
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            EntryConfig entryConfig = new EntryConfig();
            entryConfig.setEntryClass("org.safehaus.penrose.directory.ProxyEntry");

            entryConfig.addSourceConfig(new EntrySourceConfig(sourceConfig.getName()));

            entryConfig.setACL(aclPage.getACL());

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
