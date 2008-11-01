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
package org.safehaus.penrose.studio.partition.wizard;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.acl.ACI;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.directory.ProxyEntry;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.ldap.LDAP;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.jndi.connection.JNDIConnectionParametersWizardPage;
import org.safehaus.penrose.studio.ldap.connection.LDAPConnectionWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.util.ADUtil;
import org.safehaus.penrose.studio.util.SchemaUtil;

import javax.naming.InitialContext;
import java.util.Map;

/**
 * @author Endi S. Dewata
 */
public class CreateLDAPProxyWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;

    public PartitionProxyPage infoPage;
    public LDAPConnectionWizardPage connectionInfoPage;
    public JNDIConnectionParametersWizardPage connectionParametersPage;

    public CreateLDAPProxyWizard() {
        setWindowTitle("New LDAP Proxy");
    }

    public void addPages() {

        infoPage = new PartitionProxyPage();
        addPage(infoPage);

        connectionInfoPage = new LDAPConnectionWizardPage();
        addPage(connectionInfoPage);

        connectionParametersPage = new JNDIConnectionParametersWizardPage();
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
            String partitionName = infoPage.getPartitionName();

            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

            PartitionConfig partitionConfig = new PartitionConfig(partitionName);
            partitionManagerClient.createPartition(partitionConfig);

            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
            DirectoryClient directoryClient = partitionClient.getDirectoryClient();

            PenroseStudio penroseStudio = PenroseStudio.getInstance();

            ConnectionConfig connectionConfig = new ConnectionConfig();
            connectionConfig.setName(partitionName);
            connectionConfig.setAdapterName("LDAP");
            connectionConfig.setParameter(InitialContext.PROVIDER_URL, connectionInfoPage.getProviderUrl());
            connectionConfig.setParameter(InitialContext.SECURITY_PRINCIPAL, connectionInfoPage.getBindDn());
            connectionConfig.setParameter(InitialContext.SECURITY_CREDENTIALS, connectionInfoPage.getBindPassword());

            Map<String,String> parameters = connectionParametersPage.getParameters();
            for (String paramName : parameters.keySet()) {
                String paramValue = parameters.get(paramName);

                connectionConfig.setParameter(paramName, paramValue);
            }

            //partitionConfig.getConnectionConfigManager().addConnectionConfig(connectionConfig);
            connectionManagerClient.createConnection(connectionConfig);

            //SourceConfigManager sourceConfigManager = partitionConfig.getSourceConfigManager();

            SourceConfig sourceConfig = new SourceConfig();
            sourceConfig.setName(partitionName);
            sourceConfig.setConnectionName(partitionName);
            sourceConfig.setParameter("baseDn", connectionInfoPage.getSuffix());
            sourceConfig.setParameter("scope", "SUBTREE");
            sourceConfig.setParameter("filter", "(objectClass=*)");

            //sourceConfigManager.addSourceConfig(sourceConfig);
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
            sourceManagerClient.createSource(sourceConfig);

            EntryConfig rootEntry = new EntryConfig(connectionInfoPage.getSuffix());

            EntrySourceConfig sourceMapping = new EntrySourceConfig("DEFAULT", partitionName);
            rootEntry.addSourceConfig(sourceMapping);

            rootEntry.setEntryClass(ProxyEntry.class.getName());

            rootEntry.addACI(new ACI("rs"));

            //partitionConfig.getDirectoryConfig().addEntryConfig(rootEntry);
            directoryClient.createEntry(rootEntry);

            if (infoPage.getMapRootDse()) {
                SourceConfig rootDseSourceConfig = new SourceConfig();
                rootDseSourceConfig.setName(partitionName+" Root DSE");
                rootDseSourceConfig.setConnectionName(partitionName);

                rootDseSourceConfig.setParameter("scope", "OBJECT");
                rootDseSourceConfig.setParameter("filter", "objectClass=*");

                //sourceConfigManager.addSourceConfig(rootDseSourceConfig);
                sourceManagerClient.createSource(rootDseSourceConfig);

                EntryConfig rootDseEntryConfig = new EntryConfig();
                rootDseEntryConfig.setDn("");

                EntrySourceConfig rootDseSourceMapping = new EntrySourceConfig("DEFAULT", rootDseSourceConfig.getName());
                rootDseEntryConfig.addSourceConfig(rootDseSourceMapping);

                rootDseEntryConfig.setEntryClass(ProxyEntry.class.getName());

                rootDseEntryConfig.addACI(new ACI("rs"));

                //partitionConfig.getDirectoryConfig().addEntryConfig(rootDseEntryConfig);
                directoryClient.createEntry(rootDseEntryConfig);
            }

            if (infoPage.getMapADSchema()) {
                String schemaFormat = infoPage.getSchemaFormat();
                String sourceSchemaDn = "CN=Schema,CN=Configuration,"+connectionInfoPage.getSuffix();
                String destSchemaDn = LDAP.SCHEMA_DN.toString();

                EntryConfig schemaMapping;

                if (PartitionProxyPage.LDAP.equals(schemaFormat)) {
                    ADUtil util = new ADUtil();
                    schemaMapping = util.createSchemaProxy(partitionClient, connectionConfig.getName(), sourceSchemaDn, destSchemaDn);

                } else {
                    SchemaUtil util = new SchemaUtil();
                    schemaMapping = util.createSchemaProxy(partitionClient, connectionConfig.getName(), sourceSchemaDn, destSchemaDn);
                }

                schemaMapping.addACI(new ACI("rs"));
            }

            //project.save(partitionConfig);

            partitionClient.store();

            penroseStudio.notifyChangeListeners();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
