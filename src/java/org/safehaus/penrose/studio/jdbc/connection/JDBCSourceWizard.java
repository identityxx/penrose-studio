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
package org.safehaus.penrose.studio.jdbc.connection;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.studio.source.wizard.SourceWizardPage;
import org.safehaus.penrose.studio.jdbc.source.JDBCPrimaryKeyWizardPage;
import org.safehaus.penrose.studio.jdbc.source.JDBCFieldWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.Table;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.management.partition.PartitionManagerClient;
import org.safehaus.penrose.management.partition.PartitionClient;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class JDBCSourceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private String partitionName;
    private ConnectionConfig connectionConfig;
    private Table table;
    private SourceConfig sourceConfig;

    public SourceWizardPage propertyPage;
    public JDBCFieldWizardPage fieldsPage;
    public JDBCPrimaryKeyWizardPage primaryKeyPage = new JDBCPrimaryKeyWizardPage();

    public JDBCSourceWizard(String partitionName, ConnectionConfig connectionConfig, Table table) {
        this.partitionName = partitionName;
        this.connectionConfig = connectionConfig;
        this.table = table;

        propertyPage = new SourceWizardPage(table.getName().toLowerCase());
        fieldsPage = new JDBCFieldWizardPage();

        setWindowTitle(connectionConfig.getName()+" - New Source");
    }

    public boolean canFinish() {
        if (!propertyPage.isPageComplete()) return false;
        if (!fieldsPage.isPageComplete()) return false;
        if (!primaryKeyPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            sourceConfig = new SourceConfig();
            sourceConfig.setName(propertyPage.getSourceName());
            sourceConfig.setConnectionName(connectionConfig.getName());

            String catalog = table.getCatalog();
            String schema = table.getSchema();
            String table = this.table.getName();

            sourceConfig.setParameter(JDBCClient.CATALOG, catalog);
            sourceConfig.setParameter(JDBCClient.SCHEMA, schema);
            sourceConfig.setParameter(JDBCClient.TABLE, table);

            String filter = fieldsPage.getFilter();
            if (filter != null) {
                sourceConfig.setParameter(JDBCClient.FILTER, filter);
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

            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            partitionClient.createSource(sourceConfig);
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
        addPage(fieldsPage);
        addPage(primaryKeyPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (propertyPage == page) {
            fieldsPage.setTableConfig(connectionConfig, table);

        } else if (fieldsPage == page) {
            Collection<FieldConfig> selectedFields = fieldsPage.getSelectedFieldConfigs();
            primaryKeyPage.setFieldConfigs(selectedFields);
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
