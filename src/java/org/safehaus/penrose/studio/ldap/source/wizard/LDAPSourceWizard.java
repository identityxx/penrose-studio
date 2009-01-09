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
package org.safehaus.penrose.studio.ldap.source.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.source.wizard.SourceWizardPage;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.ldap.source.LDAPTreeWizardPage;
import org.safehaus.penrose.studio.ldap.source.LDAPAttributeWizardPage;
import org.safehaus.penrose.studio.ldap.source.LDAPFieldWizardPage;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class LDAPSourceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private String partitionName;
    private ConnectionConfig connectionConfig;
    private SourceConfig sourceConfig;

    public SourceWizardPage propertyPage;
    public SelectConnectionWizardPage connectionPage;

    public LDAPTreeWizardPage treePage;
    public LDAPAttributeWizardPage attributesPage;
    public LDAPFieldWizardPage fieldsPage;

    public LDAPSourceWizard() throws Exception {
        setWindowTitle("New LDAP Source");
    }

    public void addPages() {

        propertyPage = new SourceWizardPage();

        addPage(propertyPage);

        if (connectionConfig == null) {
            connectionPage = new SelectConnectionWizardPage(partitionName);
            connectionPage.setProject(server);

            addPage(connectionPage);
        }

        treePage = new LDAPTreeWizardPage();
        treePage.setConnectionConfig(connectionConfig);

        addPage(treePage);

        attributesPage = new LDAPAttributeWizardPage();
        attributesPage.setConnectionConfig(connectionConfig);

        addPage(attributesPage);

        fieldsPage = new LDAPFieldWizardPage();

        addPage(fieldsPage);
    }

    public boolean canFinish() {
        if (!propertyPage.isPageComplete()) return false;
        if (connectionPage != null && !connectionPage.isPageComplete()) return false;
        if (!treePage.isPageComplete()) return false;
        if (!attributesPage.isPageComplete()) return false;
        if (!fieldsPage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            connectionConfig = connectionPage.getConnectionConfig();
            if (connectionConfig == null) return null;
            treePage.setConnectionConfig(connectionConfig);

        } else if (treePage == page) {
            attributesPage.setConnectionConfig(connectionConfig);

        } else if (attributesPage == page) {
            Collection<AttributeType> attributeTypes = attributesPage.getAttributeTypes();
            fieldsPage.setAttributeTypes(attributeTypes);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            sourceConfig = new SourceConfig();
            sourceConfig.setName(propertyPage.getSourceName());
            sourceConfig.setConnectionName(connectionConfig.getName());

            sourceConfig.setParameter("baseDn", treePage.getBaseDn());
            sourceConfig.setParameter("filter", treePage.getFilter());
            sourceConfig.setParameter("scope", treePage.getScope());
            sourceConfig.setParameter("objectClasses", treePage.getObjectClasses());

            Collection<FieldConfig> fields = fieldsPage.getFields();
            for (FieldConfig field : fields) {
                sourceConfig.addFieldConfig(field);
            }

            PenroseClient client = server.getClient();
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

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
