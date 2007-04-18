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

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.util.ADUtil;
import org.safehaus.penrose.studio.util.SchemaUtil;
import org.safehaus.penrose.studio.connection.wizard.JNDIConnectionInfoWizardPage;
import org.safehaus.penrose.studio.connection.wizard.JNDIConnectionParametersWizardPage;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.acl.ACI;
import org.safehaus.penrose.source.Sources;
import org.apache.log4j.Logger;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Map;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class CreateLDAPProxyWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public PartitionProxyPage infoPage = new PartitionProxyPage();
    public JNDIConnectionInfoWizardPage connectionInfoPage = new JNDIConnectionInfoWizardPage();
    public JNDIConnectionParametersWizardPage connectionParametersPage = new JNDIConnectionParametersWizardPage();

    public CreateLDAPProxyWizard() {

        Map parameters = new TreeMap();
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
            String name = infoPage.getPartitionName();
            String path = "partitions/"+name;

            PartitionConfig partitionConfig = new PartitionConfig();
            partitionConfig.setName(name);
            partitionConfig.setPath(path);

            PenroseApplication penroseApplication = PenroseApplication.getInstance();
            PenroseConfig penroseConfig = penroseApplication.getPenroseConfig();
            penroseConfig.addPartitionConfig(partitionConfig);

            PartitionManager partitionManager = penroseApplication.getPartitionManager();
            Partition partition = partitionManager.load(penroseApplication.getWorkDir(), partitionConfig);

            ConnectionConfig connectionConfig = new ConnectionConfig();
            connectionConfig.setName(name);
            connectionConfig.setAdapterName("LDAP");
            connectionConfig.setParameter(InitialContext.PROVIDER_URL, connectionInfoPage.getURL()+"/");
            connectionConfig.setParameter(InitialContext.SECURITY_PRINCIPAL, connectionInfoPage.getBindDN());
            connectionConfig.setParameter(InitialContext.SECURITY_CREDENTIALS, connectionInfoPage.getPassword());

            Map parameters = connectionParametersPage.getParameters();
            for (Iterator i=parameters.keySet().iterator(); i.hasNext(); ) {
                String paramName = (String)i.next();
                String paramValue = (String)parameters.get(paramName);

                connectionConfig.setParameter(paramName, paramValue);
            }

            partition.addConnectionConfig(connectionConfig);

            Sources sources = partition.getSources();

            SourceConfig sourceConfig = new SourceConfig(name, name);
            sourceConfig.setParameter("baseDn", connectionInfoPage.getSuffix());
            sourceConfig.setParameter("scope", "SUBTREE");
            sourceConfig.setParameter("filter", "(objectClass=*)");
            sources.addSourceConfig(sourceConfig);

            EntryMapping rootEntry = new EntryMapping(connectionInfoPage.getSuffix());

            SourceMapping sourceMapping = new SourceMapping("DEFAULT", name);
            rootEntry.addSourceMapping(sourceMapping);

            HandlerMapping rootHandlerMapping = new HandlerMapping("DEFAULT", "PROXY");
            rootEntry.setHandlerMapping(rootHandlerMapping);

            rootEntry.addACI(new ACI("rs"));

            partition.addEntryMapping(rootEntry);

            if (infoPage.getMapRootDse()) {
                SourceConfig rootDseSourceConfig = new SourceConfig();
                rootDseSourceConfig.setName(name+" Root DSE");
                rootDseSourceConfig.setConnectionName(name);

                rootDseSourceConfig.setParameter("scope", "OBJECT");
                rootDseSourceConfig.setParameter("filter", "objectClass=*");

                sources.addSourceConfig(rootDseSourceConfig);

                EntryMapping rootDseEntryMapping = new EntryMapping();

                SourceMapping rootDseSourceMapping = new SourceMapping("DEFAULT", rootDseSourceConfig.getName());
                rootDseEntryMapping.addSourceMapping(rootDseSourceMapping);

                HandlerMapping rootDseHandlerMapping = new HandlerMapping("DEFAULT", "PROXY");
                rootDseEntryMapping.setHandlerMapping(rootDseHandlerMapping);

                rootDseEntryMapping.addACI(new ACI("rs"));

                partition.addEntryMapping(rootDseEntryMapping);
            }

            if (infoPage.getMapADSchema()) {
                String schemaFormat = infoPage.getSchemaFormat();
                String sourceSchemaDn = "CN=Schema,CN=Configuration,"+connectionInfoPage.getSuffix();
                String destSchemaDn = "cn=schema,ou=system";

                EntryMapping schemaMapping;

                if (PartitionProxyPage.LDAP.equals(schemaFormat)) {
                    ADUtil util = new ADUtil();
                    schemaMapping = util.createSchemaProxy(partition, connectionConfig, sourceSchemaDn, destSchemaDn);

                } else {
                    SchemaUtil util = new SchemaUtil();
                    schemaMapping = util.createSchemaProxy(partition, connectionConfig, sourceSchemaDn, destSchemaDn);
                }

                schemaMapping.addACI(new ACI("rs"));
            }

            //AdapterConfig adapterConfig = penroseConfig.getAdapterConfig("LDAP");

            //ConnectionManager connectionManager = penroseApplication.getConnectionManager();
            //connectionManager.init(partition, connectionConfig, adapterConfig);

            penroseApplication.notifyChangeListeners();

            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return false;
        }
    }
}
