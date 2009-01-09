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

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.jdbc.Table;
import org.safehaus.penrose.jdbc.source.JDBCSource;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.source.wizard.SourceWizardPage;
import org.safehaus.penrose.studio.jdbc.source.JDBCTableWizardPage;
import org.safehaus.penrose.studio.jdbc.source.JDBCFieldWizardPage;
import org.safehaus.penrose.studio.jdbc.source.JDBCPrimaryKeyWizardPage;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class JDBCSourceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private String partitionName;
    private ConnectionConfig connectionConfig;
    private SourceConfig sourceConfig;

    public SourceWizardPage propertyPage;
    public SelectConnectionWizardPage connectionPage;

    public JDBCTableWizardPage tablePage;
    public JDBCFieldWizardPage fieldsPage;
    public JDBCPrimaryKeyWizardPage primaryKeyPage;

    public JDBCSourceWizard() {
        setWindowTitle("New JDBC Source");
    }

    public void addPages() {
        propertyPage = new SourceWizardPage();

        addPage(propertyPage);

        if (connectionConfig == null) {
            connectionPage = new SelectConnectionWizardPage(partitionName);
            connectionPage.setProject(server);

            addPage(connectionPage);
        }

        tablePage = new JDBCTableWizardPage();
        tablePage.setServer(server);
        tablePage.setPartitionName(partitionName);
        tablePage.setConnectionConfig(connectionConfig);

        addPage(tablePage);

        fieldsPage = new JDBCFieldWizardPage();
        fieldsPage.setServer(server);
        fieldsPage.setPartitionName(partitionName);
        fieldsPage.setConnectionConfig(connectionConfig);

        addPage(fieldsPage);

        primaryKeyPage = new JDBCPrimaryKeyWizardPage();

        addPage(primaryKeyPage);
    }

    public boolean canFinish() {
        if (!propertyPage.isPageComplete()) return false;
        if (connectionPage != null && !connectionPage.isPageComplete()) return false;
        if (!tablePage.isPageComplete()) return false;
        if (!fieldsPage.isPageComplete()) return false;
        if (!primaryKeyPage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            connectionConfig = connectionPage.getConnectionConfig();
            if (connectionConfig == null) return null;
            tablePage.setConnectionConfig(connectionConfig);

        } else if (tablePage == page) {
            fieldsPage.setConnectionConfig(connectionConfig);
            fieldsPage.setTable(tablePage.getTable());

        } else if (fieldsPage == page) {
            Collection<FieldConfig> selectedFields = fieldsPage.getSelectedFieldConfigs();
            primaryKeyPage.setFieldConfigs(selectedFields);
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            sourceConfig = new SourceConfig();
            sourceConfig.setName(propertyPage.getSourceName());
            sourceConfig.setConnectionName(connectionConfig.getName());

            Table table = tablePage.getTable();

            String catalog   = table.getCatalog();
            String schema    = table.getSchema();
            String tableName = table.getName();

            sourceConfig.setParameter(JDBCSource.CATALOG, catalog);
            sourceConfig.setParameter(JDBCSource.SCHEMA, schema);
            sourceConfig.setParameter(JDBCSource.TABLE, tableName);

            String filter = fieldsPage.getFilter();
            if (filter != null) {
                sourceConfig.setParameter(JDBCSource.FILTER, filter);
            }

            System.out.println("Saving fields :");
            Collection<FieldConfig> fields = primaryKeyPage.getFields();
            if (fields.isEmpty()) {
                fields = fieldsPage.getSelectedFieldConfigs();
            }

            for (FieldConfig field : fields) {
                System.out.println(" - " + field.getName() + " " + field.isPrimaryKey());
                sourceConfig.addFieldConfig(field);
            }

            //SourceConfigManager sourceConfigManager = partitionConfig.getSourceConfigManager();
            //sourceConfigManager.addSourceConfig(sourceConfig);
            //project.save(partitionConfig, sourceConfigManager);

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
