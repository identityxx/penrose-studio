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
package org.safehaus.penrose.studio.ldap.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.acl.ACI;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.directory.*;
import org.safehaus.penrose.ldap.LDAP;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.config.wizard.ParametersWizardPage;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.ldap.connection.wizard.LDAPConnectionSettingsWizardPage;
import org.safehaus.penrose.studio.util.ADUtil;
import org.safehaus.penrose.studio.util.SchemaUtil;
import org.safehaus.penrose.studio.partition.wizard.PartitionProxyPage;

import javax.naming.InitialContext;
import java.util.Map;

/**
 * @author Endi S. Dewata
 */
public class LDAPProxyWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;

    public PartitionProxyPage infoPage;
    public LDAPConnectionSettingsWizardPage connectionInfoPage;
    public ParametersWizardPage connectionParametersPage;

    public LDAPProxyWizard() {
        setWindowTitle("New LDAP Proxy");
    }

    public void addPages() {

        infoPage = new PartitionProxyPage();
        addPage(infoPage);

        connectionInfoPage = new LDAPConnectionSettingsWizardPage();
        addPage(connectionInfoPage);

        connectionParametersPage = new ParametersWizardPage();
        addPage(connectionParametersPage);
    }

    public boolean canFinish() {
        if (!infoPage.isPageComplete()) return false;
        if (!connectionInfoPage.isPageComplete()) return false;
        if (!connectionParametersPage.isPageComplete()) return false;
        return true;
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public boolean performFinish() {
        try {
            String name = infoPage.getPartitionName();

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

            PartitionClient partitionClient = partitionManagerClient.getPartitionClient("DEFAULT");

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Connection
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

            ConnectionConfig connectionConfig = new ConnectionConfig();
            connectionConfig.setName(name);
            connectionConfig.setAdapterName("LDAP");
            connectionConfig.setParameter(InitialContext.PROVIDER_URL, connectionInfoPage.getProviderUrl());
            connectionConfig.setParameter(InitialContext.SECURITY_PRINCIPAL, connectionInfoPage.getBindDn());
            connectionConfig.setParameter(InitialContext.SECURITY_CREDENTIALS, connectionInfoPage.getBindPassword());

            Map<String,String> parameters = connectionParametersPage.getParameters();
            for (String paramName : parameters.keySet()) {
                String paramValue = parameters.get(paramName);

                connectionConfig.setParameter(paramName, paramValue);
            }

            connectionManagerClient.createConnection(connectionConfig);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Source
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            SourceConfig sourceConfig = new SourceConfig();
            sourceConfig.setName(name);
            sourceConfig.setConnectionName(name);
            sourceConfig.setParameter("baseDn", connectionInfoPage.getSuffix());

            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
            sourceManagerClient.createSource(sourceConfig);

            createDirectoryProxy(name, partitionClient);

            if (infoPage.getMapRootDse()) {
                createRootDseProxy(name, partitionClient);
            }

            if (infoPage.getMapADSchema()) {
                createSchemaProxy(name, partitionClient);
            }

            partitionClient.store();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void createDirectoryProxy(String name, PartitionClient partitionClient) throws Exception {

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Directory
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        EntryConfig rootEntry = new EntryConfig(connectionInfoPage.getSuffix());

        EntrySourceConfig sourceReference = new EntrySourceConfig(name);
        rootEntry.addSourceConfig(sourceReference);

        rootEntry.setEntryClass(ProxyEntry.class.getName());

        rootEntry.addACI(new ACI("rs"));

        DirectoryClient directoryClient = partitionClient.getDirectoryClient();
        directoryClient.createEntry(rootEntry);
    }

    public void createRootDseProxy(String name, PartitionClient partitionClient) throws Exception {

        DirectoryClient directoryClient = partitionClient.getDirectoryClient();

        DN dn = new DN();

        String entryName = directoryClient.getEntryName(dn);
        EntryConfig entryConfig;
        
        if (entryName == null) {
            entryConfig = new EntryConfig(dn);
            
        } else {
            EntryClient rootDseClient = directoryClient.getEntryClient(entryName);
            entryConfig = rootDseClient.getEntryConfig();

            entryConfig.removeObjectClasses();
            entryConfig.removeAttributeConfigs();
            entryConfig.removeSourceConfigs();
            entryConfig.removeACL();
            entryConfig.removeParameters();
        }

        entryConfig.setEntryClass(ProxyEntry.class.getName());

        EntrySourceConfig sourceReference = new EntrySourceConfig(name);
        entryConfig.addSourceConfig(sourceReference);

        entryConfig.addACI(new ACI("rs"));

        if (entryName == null) {
            directoryClient.createEntry(entryConfig);
        } else {
            directoryClient.updateEntry(entryName, entryConfig);
        }
    }

    public void createSchemaProxy(String name, PartitionClient partitionClient) throws Exception {

        String schemaFormat = infoPage.getSchemaFormat();
        String sourceSchemaDn = "CN=Schema,CN=Configuration,"+connectionInfoPage.getSuffix();
        String destSchemaDn = LDAP.SCHEMA_DN.toString();

        if (PartitionProxyPage.LDAP.equals(schemaFormat)) {
            ADUtil util = new ADUtil();
            util.createSchemaProxy(partitionClient, name, sourceSchemaDn, destSchemaDn);

        } else {
            SchemaUtil util = new SchemaUtil();
            util.createSchemaProxy(partitionClient, name, sourceSchemaDn, destSchemaDn);
        }
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
