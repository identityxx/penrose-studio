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
package org.safehaus.penrose.studio.ldap.connection.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.ldap.source.wizard.LDAPSourceFieldsWizardPage;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.source.wizard.SourcePrimaryKeysWizardPage;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.ldap.LDAPClient;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.source.LDAPSource;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class LDAPSourceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server project;
    private LDAPClient client;
    private String partitionName;
    private ConnectionConfig connectionConfig;
    private String baseDn;
    private String filter;
    private String scope;
    private Collection attributeNames;
    private SourceConfig sourceConfig;

    public LDAPSourceWizardPage propertiesPage;
    public LDAPSourceFieldsWizardPage fieldsPage;
    public SourcePrimaryKeysWizardPage primarykeysPage = new SourcePrimaryKeysWizardPage();

    Map<String,FieldConfig> availableFieldConfigs = new TreeMap<String,FieldConfig>();
    Map<String,FieldConfig> selectedFieldConfigs = new TreeMap<String,FieldConfig>();

    public LDAPSourceWizard(LDAPClient client, String partitionName, ConnectionConfig connectionConfig, String baseDn) throws Exception {
        this(client, partitionName, connectionConfig, baseDn, "(objectClass=*)", "OBJECT", new ArrayList<String>());
    }
    
    public LDAPSourceWizard(
            LDAPClient client,
            String partitionName,
            ConnectionConfig connectionConfig,
            String baseDn,
            String filter,
            String scope,
            Collection<String> attributeNames) throws Exception {

        this.client = client;
        this.partitionName = partitionName;
        this.connectionConfig = connectionConfig;
        this.baseDn = baseDn;
        this.filter = filter;
        this.scope = scope;
        this.attributeNames = attributeNames;

        RDN rdn = new DN(baseDn).getRdn();
        String rdnAttr = rdn.getNames().iterator().next();
        String rdnValue = (String)rdn.get(rdnAttr);
        String name = rdnValue.replaceAll("\\s", "").toLowerCase();

        propertiesPage = new LDAPSourceWizardPage(name, baseDn, filter, scope);
        
        fieldsPage = new LDAPSourceFieldsWizardPage(attributeNames);
        //fieldsPage.setMappingConfig(connectionConfig);

        setWindowTitle(connectionConfig.getName()+" - New Source");
    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;
        if (!fieldsPage.isPageComplete()) return false;
        if (!primarykeysPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            sourceConfig = new SourceConfig();
            sourceConfig.setName(propertiesPage.getSourceName());
            sourceConfig.setConnectionName(connectionConfig.getName());

            sourceConfig.setParameter(LDAPSource.BASE_DN, baseDn);
            sourceConfig.setParameter(LDAPSource.FILTER, propertiesPage.getFilter());
            sourceConfig.setParameter(LDAPSource.SCOPE, propertiesPage.getScope());

            sourceConfig.setFieldConfigs(selectedFieldConfigs.values());

            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
            sourceManagerClient.createSource(sourceConfig);
            partitionClient.store();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig connection) {
        this.sourceConfig = connection;
    }

    public void addPages() {
        addPage(propertiesPage);
        addPage(fieldsPage);
        addPage(primarykeysPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (fieldsPage == page) {
            try {
                RDN rdn = new DN(baseDn).getRdn();
                Collection<String> names = new ArrayList<String>();
                for (String name : rdn.getNames()) {
                    names.add(name.toLowerCase());
                }
                //Collection<AttributeType> attributeTypes = fieldsPage.getAttributeTypes();
                //primarykeysPage.setAttributeTypes(attributeTypes, names);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return super.getNextPage(page);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public Server getProject() {
        return project;
    }

    public void setProject(Server project) {
        this.project = project;
    }
}
