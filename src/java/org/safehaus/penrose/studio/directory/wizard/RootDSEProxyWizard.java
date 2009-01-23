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
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.acl.ACI;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.directory.ProxyEntry;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class RootDSEProxyWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public SelectConnectionWizardPage connectionPage;

    private Server server;
    private String partitionName;

    public RootDSEProxyWizard() {
        setWindowTitle("New Root DSE Proxy");
    }

    public void addPages() {

        connectionPage = new SelectConnectionWizardPage();
        connectionPage.setServer(server);
        connectionPage.setPartitionName(partitionName);
        connectionPage.setAdapterType("LDAP");

        addPage(connectionPage);
    }

    public boolean canFinish() {
        return connectionPage.isPageComplete();
    }

    public boolean performFinish() {
        try {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();

            SourceConfig sourceConfig = new SourceConfig();
            sourceConfig.setName(connectionConfig.getName()+" Root DSE");
            sourceConfig.setConnectionName(connectionConfig.getName());

            sourceConfig.setParameter("scope", "OBJECT");
            sourceConfig.setParameter("filter", "objectClass=*");

            //SourceConfigManager sourceConfigManager = partitionConfig.getSourceConfigManager();
            //sourceConfigManager.addSourceConfig(sourceConfig);

            EntryConfig entryConfig = new EntryConfig();

            EntrySourceConfig sourceMapping = new EntrySourceConfig(sourceConfig.getName());
            entryConfig.addSourceConfig(sourceMapping);

            entryConfig.setEntryClass(ProxyEntry.class.getName());

            entryConfig.addACI(new ACI("rs"));

            //DirectoryConfig directoryConfig = partitionConfig.getDirectoryConfig();
            //directoryConfig.addEntryConfig(entryConfig);

            //project.save(partitionConfig, sourceConfigManager);
            //project.save(partitionConfig, directoryConfig);

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
            sourceManagerClient.createSource(sourceConfig);

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
