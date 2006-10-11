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
package org.safehaus.penrose.studio.connection;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.connector.JDBCAdapter;
import org.safehaus.penrose.studio.source.wizard.SourceWizardPage;
import org.safehaus.penrose.studio.source.wizard.JDBCPrimaryKeyWizardPage;
import org.safehaus.penrose.studio.source.wizard.*;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class JDBCSourceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Partition partition;
    private ConnectionConfig connectionConfig;
    private TableConfig tableConfig;
    private SourceConfig sourceConfig;

    public SourceWizardPage propertyPage;
    public JDBCFieldWizardPage fieldsPage;
    public JDBCPrimaryKeyWizardPage primaryKeyPage = new JDBCPrimaryKeyWizardPage();

    public JDBCSourceWizard(Partition partition, ConnectionConfig connectionConfig, TableConfig tableConfig) {
        this.partition = partition;
        this.connectionConfig = connectionConfig;
        this.tableConfig = tableConfig;

        propertyPage = new SourceWizardPage(tableConfig.getName().toLowerCase());
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

            String tableName = tableConfig.getName();

            String schema = tableConfig.getSchema();
            if (schema != null) {
                tableName = schema+"."+tableName;
            }

            String catalog = tableConfig.getCatalog();
            if (catalog != null) {
                tableName = catalog+"."+tableName;
            }

            sourceConfig.setParameter(JDBCAdapter.TABLE_NAME, tableName);

            String filter = fieldsPage.getFilter();
            if (filter != null) {
                sourceConfig.setParameter(JDBCAdapter.FILTER, filter);
            }

            System.out.println("Saving fields :");
            Collection fields = primaryKeyPage.getFields();
            if (fields.isEmpty()) {
                fields = fieldsPage.getSelectedFieldConfigs();
            }

            for (Iterator i=fields.iterator(); i.hasNext(); ) {
                FieldConfig field = (FieldConfig)i.next();
                System.out.println(" - "+field.getName()+" "+field.isPK());
                sourceConfig.addFieldConfig(field);
            }

            partition.addSourceConfig(sourceConfig);

            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
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
            fieldsPage.setTableConfig(connectionConfig, tableConfig);

        } else if (fieldsPage == page) {
            Collection selectedFields = fieldsPage.getSelectedFieldConfigs();
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

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
