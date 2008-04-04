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
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.SourceMapping;
import org.safehaus.penrose.ldap.LDAP;
import org.safehaus.penrose.management.partition.PartitionClient;
import org.safehaus.penrose.management.partition.PartitionManagerClient;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.jndi.connection.JNDIConnectionParametersWizardPage;
import org.safehaus.penrose.studio.ldap.connection.LDAPConnectionWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.util.ADUtil;
import org.safehaus.penrose.studio.util.SchemaUtil;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class CreateLDAPProxyWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;

    public PartitionProxyPage infoPage = new PartitionProxyPage();
    public LDAPConnectionWizardPage connectionInfoPage = new LDAPConnectionWizardPage();
    public JNDIConnectionParametersWizardPage connectionParametersPage = new JNDIConnectionParametersWizardPage();

    public CreateLDAPProxyWizard() {

        Map<String,String> parameters = new TreeMap<String,String>();
        parameters.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        connectionParametersPage.setParameters(parameters);

        setWindowTitle("New LDAP Proxy");
    }

    public boolean canFinish() {
        if (!infoPage.isPageComplete()) return false;
        if (!connectionInfoPage.isPageComplete()) return false;
        if (!connectionParametersPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        addPage(infoPage);
        addPage(connectionInfoPage);
        addPage(connectionParametersPage);
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

            PenroseStudio penroseStudio = PenroseStudio.getInstance();

            ConnectionConfig connectionConfig = new ConnectionConfig();
            connectionConfig.setName(partitionName);
            connectionConfig.setAdapterName("LDAP");
            connectionConfig.setParameter(InitialContext.PROVIDER_URL, connectionInfoPage.getProviderUrl());
            connectionConfig.setParameter(InitialContext.SECURITY_PRINCIPAL, connectionInfoPage.getBindDN());
            connectionConfig.setParameter(InitialContext.SECURITY_CREDENTIALS, connectionInfoPage.getPassword());

            Map<String,String> parameters = connectionParametersPage.getParameters();
            for (String paramName : parameters.keySet()) {
                String paramValue = parameters.get(paramName);

                connectionConfig.setParameter(paramName, paramValue);
            }

            //partitionConfig.getConnectionConfigManager().addConnectionConfig(connectionConfig);
            partitionClient.createConnection(connectionConfig);

            //SourceConfigManager sourceConfigManager = partitionConfig.getSourceConfigManager();

            SourceConfig sourceConfig = new SourceConfig(partitionName, partitionName);
            sourceConfig.setParameter("baseDn", connectionInfoPage.getSuffix());
            sourceConfig.setParameter("scope", "SUBTREE");
            sourceConfig.setParameter("filter", "(objectClass=*)");

            //sourceConfigManager.addSourceConfig(sourceConfig);
            partitionClient.createSource(sourceConfig);

            EntryConfig rootEntry = new EntryConfig(connectionInfoPage.getSuffix());

            SourceMapping sourceMapping = new SourceMapping("DEFAULT", partitionName);
            rootEntry.addSourceMapping(sourceMapping);

            rootEntry.setHandlerName("PROXY");

            rootEntry.addACI(new ACI("rs"));

            //partitionConfig.getDirectoryConfig().addEntryConfig(rootEntry);
            partitionClient.createEntry(rootEntry);

            if (infoPage.getMapRootDse()) {
                SourceConfig rootDseSourceConfig = new SourceConfig();
                rootDseSourceConfig.setName(partitionName+" Root DSE");
                rootDseSourceConfig.setConnectionName(partitionName);

                rootDseSourceConfig.setParameter("scope", "OBJECT");
                rootDseSourceConfig.setParameter("filter", "objectClass=*");

                //sourceConfigManager.addSourceConfig(rootDseSourceConfig);
                partitionClient.createSource(rootDseSourceConfig);

                EntryConfig rootDseEntryConfig = new EntryConfig();
                rootDseEntryConfig.setDn("");

                SourceMapping rootDseSourceMapping = new SourceMapping("DEFAULT", rootDseSourceConfig.getName());
                rootDseEntryConfig.addSourceMapping(rootDseSourceMapping);

                rootDseEntryConfig.setHandlerName("PROXY");

                rootDseEntryConfig.addACI(new ACI("rs"));

                //partitionConfig.getDirectoryConfig().addEntryConfig(rootDseEntryConfig);
                partitionClient.createEntry(rootDseEntryConfig);
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
