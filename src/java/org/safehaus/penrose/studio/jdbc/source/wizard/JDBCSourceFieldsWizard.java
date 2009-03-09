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

import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.source.FieldConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.source.wizard.SourceWizard;
import org.safehaus.penrose.studio.source.wizard.SourcePrimaryKeysWizardPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.jdbc.Table;
import org.safehaus.penrose.jdbc.JDBC;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class JDBCSourceFieldsWizard extends SourceWizard {

    public JDBCSourceFieldsWizardPage fieldsPage;
    public SourcePrimaryKeysWizardPage primaryKeysPage;

    Map<String,FieldConfig> availableFieldConfigs = new TreeMap<String,FieldConfig>();
    Map<String,FieldConfig> selectedFieldConfigs = new TreeMap<String,FieldConfig>();

    public JDBCSourceFieldsWizard() {
        setWindowTitle("Edit JDBC Source Fields");
    }

    public void addPages() {

        String catalog = sourceConfig.getParameter(JDBC.CATALOG);
        String schema = sourceConfig.getParameter(JDBC.SCHEMA);
        String tableName = sourceConfig.getParameter(JDBC.TABLE);

        Table table = new Table(tableName, catalog, schema);

        for (FieldConfig fieldConfig : getAvailableFieldConfigs(table)) {
            availableFieldConfigs.put(fieldConfig.getName(), fieldConfig);
        }

        for (FieldConfig fieldConfig : sourceConfig.getFieldConfigs()) {
            selectedFieldConfigs.put(fieldConfig.getName(), fieldConfig);
        }

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
            ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(sourceConfig.getConnectionName());

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
        if (!fieldsPage.isPageComplete()) return false;
        if (!primaryKeysPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            sourceConfig.setFieldConfigs(selectedFieldConfigs.values());

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
            sourceManagerClient.updateSource(sourceConfig.getName(), sourceConfig);
            partitionClient.store();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }
}