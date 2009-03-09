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
package org.safehaus.penrose.studio.jdbc.connection.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.source.wizard.SourcePropertiesWizardPage;
import org.safehaus.penrose.studio.source.wizard.SourcePrimaryKeysWizardPage;
import org.safehaus.penrose.studio.jdbc.source.wizard.JDBCSourceFieldsWizardPage;
import org.safehaus.penrose.studio.jdbc.source.wizard.JDBCSourceFilterWizardPage;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.jdbc.Table;
import org.safehaus.penrose.jdbc.JDBC;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.connection.ConnectionClient;
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
public class JDBCSourceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private String partitionName;
    private ConnectionConfig connectionConfig;
    private Table table;
    private SourceConfig sourceConfig;

    public SourcePropertiesWizardPage propertiesPage;
    public JDBCSourceFilterWizardPage filterPage;
    public JDBCSourceFieldsWizardPage fieldsPage;
    public SourcePrimaryKeysWizardPage primaryKeysPage;

    Map<String,FieldConfig> availableFieldConfigs = new TreeMap<String,FieldConfig>();
    Map<String,FieldConfig> selectedFieldConfigs = new TreeMap<String,FieldConfig>();

    public JDBCSourceWizard(String partitionName, ConnectionConfig connectionConfig, Table table) {
        this.partitionName = partitionName;
        this.connectionConfig = connectionConfig;
        this.table = table;

        setWindowTitle(connectionConfig.getName()+" - New Source");
    }

    public void addPages() {

        for (FieldConfig fieldConfig : getAvailableFieldConfigs(table)) {
            availableFieldConfigs.put(fieldConfig.getName(), fieldConfig);
        }

        propertiesPage = new SourcePropertiesWizardPage();
        propertiesPage.setSourceName(table.getName().toLowerCase());

        addPage(propertiesPage);

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
        if (!filterPage.isPageComplete()) return false;
        if (!fieldsPage.isPageComplete()) return false;
        if (!primaryKeysPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            sourceConfig = new SourceConfig();
            sourceConfig.setName(propertiesPage.getSourceName());
            sourceConfig.setSourceClass(propertiesPage.getClassName());
            sourceConfig.setEnabled(propertiesPage.isEnabled());
            sourceConfig.setDescription(propertiesPage.getSourceDescription());

            sourceConfig.setConnectionName(connectionConfig.getName());

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
            ErrorDialog.open(e);
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
