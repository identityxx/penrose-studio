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
package org.safehaus.penrose.studio.jdbc.source.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.jdbc.Table;
import org.safehaus.penrose.jdbc.JDBC;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.source.wizard.SourceWizard;
import org.safehaus.penrose.studio.source.wizard.SourcePropertiesWizardPage;
import org.safehaus.penrose.studio.source.wizard.SourcePrimaryKeysWizardPage;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.connection.ConnectionClient;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class JDBCSourceWizard extends SourceWizard {

    public SourcePropertiesWizardPage propertiesPage;

    public SelectConnectionWizardPage connectionPage;
    public JDBCSourceTableWizardPage tablePage;
    public JDBCSourceFilterWizardPage filterPage;

    public JDBCSourceFieldsWizardPage fieldsPage;
    public SourcePrimaryKeysWizardPage primaryKeysPage;

    Map<String,FieldConfig> availableFieldConfigs = new TreeMap<String,FieldConfig>();
    Map<String,FieldConfig> selectedFieldConfigs = new TreeMap<String,FieldConfig>();

    public JDBCSourceWizard() {
        setWindowTitle("New JDBC Source");
    }

    public void addPages() {

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
            connectionPage.setAdapterType("JDBC");
            connectionPage.setConnectionName(sourceConfig.getConnectionName());

            addPage(connectionPage);
        }

        tablePage = new JDBCSourceTableWizardPage();
        tablePage.setServer(server);
        tablePage.setPartitionName(partitionName);

        addPage(tablePage);

        filterPage = new JDBCSourceFilterWizardPage();

        addPage(filterPage);

        fieldsPage = new JDBCSourceFieldsWizardPage();
        fieldsPage.setAvailableFieldConfigs(availableFieldConfigs);
        fieldsPage.setSelectedFieldConfigs(selectedFieldConfigs);

        addPage(fieldsPage);

        primaryKeysPage = new SourcePrimaryKeysWizardPage();
        primaryKeysPage.setFieldConfigs(selectedFieldConfigs);

        addPage(primaryKeysPage);
    }

    public Collection<FieldConfig> getAvailableFieldConfigs(Table table) {

        Collection<FieldConfig> results = new ArrayList<FieldConfig>();

        try {
            String catalog = table.getCatalog();
            String schema = table.getSchema();
            String tableName = table.getName();

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
            ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(connectionConfig.getName());

            Collection<FieldConfig> list = (Collection<FieldConfig>)connectionClient.invoke(
                    "getColumns",
                    new Object[] { catalog, schema, tableName },
                    new String[] { String.class.getName(), String.class.getName(), String.class.getName() }
            );

            if (list != null) results.addAll(list);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return results;
    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;
        if (connectionPage != null && !connectionPage.isPageComplete()) return false;
        if (!tablePage.isPageComplete()) return false;
        if (!filterPage.isPageComplete()) return false;
        if (!fieldsPage.isPageComplete()) return false;
        if (!primaryKeysPage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            connectionConfig = connectionPage.getConnectionConfig();
            if (connectionConfig == null) return null;
            tablePage.setConnectionName(connectionConfig.getName());

        } else if (tablePage == page) {
            Table table = tablePage.getTable();

            availableFieldConfigs.clear();
            for (FieldConfig fieldConfig : getAvailableFieldConfigs(table)) {
                availableFieldConfigs.put(fieldConfig.getName(), fieldConfig);
            }
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            sourceConfig.setName(propertiesPage.getSourceName());
            sourceConfig.setSourceClass(propertiesPage.getClassName());
            sourceConfig.setEnabled(propertiesPage.isEnabled());
            sourceConfig.setDescription(propertiesPage.getSourceDescription());

            sourceConfig.setConnectionName(connectionConfig.getName());

            Table table = tablePage.getTable();

            sourceConfig.setParameter(JDBC.CATALOG, table.getCatalog());
            sourceConfig.setParameter(JDBC.SCHEMA, table.getSchema());
            sourceConfig.setParameter(JDBC.TABLE, table.getName());

            sourceConfig.setParameter(JDBC.FILTER, filterPage.getFilter());

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
            return false;
        }
    }
}
