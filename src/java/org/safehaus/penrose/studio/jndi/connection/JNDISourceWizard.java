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
package org.safehaus.penrose.studio.jndi.connection;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.jndi.source.JNDIFieldWizardPage;
import org.safehaus.penrose.studio.jndi.source.JNDIAttributeWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.ldap.LDAPClient;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class JNDISourceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private LDAPClient client;
    private String partitionName;
    private ConnectionConfig connectionConfig;
    private String baseDn;
    private String filter;
    private String scope;
    private Collection attributeNames;
    private SourceConfig sourceConfig;

    public JNDISourceWizardPage propertyPage;
    public JNDIAttributeWizardPage attributesPage;
    public JNDIFieldWizardPage fieldsPage = new JNDIFieldWizardPage();

    public JNDISourceWizard(LDAPClient client, String partitionName, ConnectionConfig connectionConfig, String baseDn) throws Exception {
        this(client, partitionName, connectionConfig, baseDn, "(objectClass=*)", "OBJECT", new ArrayList<String>());
    }
    
    public JNDISourceWizard(
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

        propertyPage = new JNDISourceWizardPage(name, baseDn, filter, scope);
        
        attributesPage = new JNDIAttributeWizardPage(attributeNames);
        attributesPage.setConnectionConfig(connectionConfig);

        setWindowTitle(connectionConfig.getName()+" - New Source");
    }

    public boolean canFinish() {
        if (!propertyPage.isPageComplete()) return false;
        if (!attributesPage.isPageComplete()) return false;
        if (!fieldsPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            sourceConfig = new SourceConfig();
            sourceConfig.setName(propertyPage.getSourceName());
            sourceConfig.setConnectionName(connectionConfig.getName());

            sourceConfig.setParameter("baseDn", baseDn);
            sourceConfig.setParameter("filter", propertyPage.getFilter());
            sourceConfig.setParameter("scope", propertyPage.getScope());

            Collection<FieldConfig> fields = fieldsPage.getFields();
            for (FieldConfig field : fields) {
                sourceConfig.addFieldConfig(field);
            }

            //SourceConfigManager sourceConfigManager = partitionConfig.getSourceConfigManager();
            //sourceConfigManager.addSourceConfig(sourceConfig);
            //project.save(partitionConfig, sourceConfigManager);

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
        addPage(propertyPage);
        addPage(attributesPage);
        addPage(fieldsPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (attributesPage == page) {
            RDN rdn = new DN(baseDn).getRdn();
            Collection<String> names = new ArrayList<String>();
            for (String name : rdn.getNames()) {
                names.add(name.toLowerCase());
            }
            Collection<AttributeType> attributeTypes = attributesPage.getAttributeTypes();
            fieldsPage.setAttributeTypes(attributeTypes, names);
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
