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

import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.source.wizard.SourceWizard;
import org.safehaus.penrose.studio.source.wizard.SourcePropertiesWizardPage;
import org.safehaus.penrose.studio.source.wizard.SourcePrimaryKeysWizardPage;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.ldap.LDAP;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class LDAPSourceWizard extends SourceWizard {

    public SourcePropertiesWizardPage propertiesPage;

    public SelectConnectionWizardPage connectionPage;
    public LDAPSourceTreeWizardPage treePage;

    public LDAPSourceFieldsWizardPage fieldsPage;
    public SourcePrimaryKeysWizardPage primaryKeysPage;

    Map<String,FieldConfig> availableFieldConfigs = new TreeMap<String,FieldConfig>();
    Map<String,FieldConfig> selectedFieldConfigs = new TreeMap<String,FieldConfig>();

    public LDAPSourceWizard() throws Exception {
        setWindowTitle("New LDAP Source");
    }

    public void addPages() {
        try {
            propertiesPage = new SourcePropertiesWizardPage();

            propertiesPage.setSourceName(sourceConfig.getName());
            propertiesPage.setClassName(sourceConfig.getSourceClass());
            propertiesPage.setEnabled(sourceConfig.isEnabled());
            propertiesPage.setSourceDescription(sourceConfig.getDescription());

            addPage(propertiesPage);

            if (connectionConfig == null) {
                connectionPage = new SelectConnectionWizardPage();
                connectionPage.setServer(server);
                connectionPage.setPartitionName(partitionName);
                connectionPage.setAdapterType("LDAP");

                addPage(connectionPage);
            }

            treePage = new LDAPSourceTreeWizardPage();
            treePage.setServer(server);
            treePage.setPartitionName(partitionName);
            treePage.setConnectionName(sourceConfig.getConnectionName());

            treePage.init();

            addPage(treePage);

            fieldsPage = new LDAPSourceFieldsWizardPage();
            fieldsPage.setServer(server);
            fieldsPage.setPartitionName(partitionName);
            fieldsPage.setAvailableFieldConfigs(availableFieldConfigs);
            fieldsPage.setSelectedFieldConfigs(selectedFieldConfigs);

            addPage(fieldsPage);

            primaryKeysPage = new SourcePrimaryKeysWizardPage();
            primaryKeysPage.setFieldConfigs(selectedFieldConfigs);

            addPage(primaryKeysPage);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;
        if (connectionPage != null && !connectionPage.isPageComplete()) return false;
        if (!treePage.isPageComplete()) return false;
        if (!fieldsPage.isPageComplete()) return false;
        if (!primaryKeysPage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            connectionConfig = connectionPage.getConnectionConfig();
            if (connectionConfig == null) return null;
            treePage.setConnectionName(connectionPage.getConnectionName());
            fieldsPage.setConnectionName(connectionPage.getConnectionName());
/*
        } else if (fieldsPage == page) {
            Collection<AttributeType> attributeTypes = fieldsPage.getAttributeTypes();
            primaryKeysPage.setAttributeTypes(attributeTypes);
*/
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            sourceConfig = new SourceConfig();
            sourceConfig.setName(propertiesPage.getSourceName());
            sourceConfig.setSourceClass(propertiesPage.getClassName());
            sourceConfig.setEnabled(propertiesPage.isEnabled());
            sourceConfig.setDescription(propertiesPage.getSourceDescription());

            sourceConfig.setConnectionName(connectionConfig.getName());

            sourceConfig.setParameter(LDAP.BASE_DN, treePage.getBaseDn());
            sourceConfig.setParameter(LDAP.FILTER, treePage.getFilter());
            sourceConfig.setParameter(LDAP.SCOPE, treePage.getScope());
            sourceConfig.setParameter(LDAP.OBJECT_CLASSES, treePage.getObjectClasses());

            sourceConfig.setFieldConfigs(selectedFieldConfigs.values());

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
            sourceManagerClient.createSource(sourceConfig);
            partitionClient.store();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }
}
