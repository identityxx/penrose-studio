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
package org.safehaus.penrose.studio.source.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.connector.JDBCAdapter;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class NewSourceWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Partition partition;
    private SourceConfig sourceConfig;

    public SourceWizardPage propertyPage;
    public SelectConnectionWizardPage connectionPage;

    public JDBCTableWizardPage jdbcTablePage;
    public JDBCFieldWizardPage jdbcFieldsPage;
    public JDBCPrimaryKeyWizardPage jdbcPrimaryKeyPage;

    public LDAPTreeWizardPage ldapTreePage;
    public LDAPAttributeWizardPage ldapAttributesPage;
    public LDAPFieldWizardPage ldapFieldsPage;

    public NewSourceWizard(Partition partition) throws Exception {
        this.partition = partition;

        propertyPage = new SourceWizardPage();
        connectionPage = new SelectConnectionWizardPage(partition);

        jdbcTablePage = new JDBCTableWizardPage();
        jdbcFieldsPage = new JDBCFieldWizardPage();
        jdbcPrimaryKeyPage = new JDBCPrimaryKeyWizardPage();

        ldapTreePage = new LDAPTreeWizardPage();
        ldapAttributesPage = new LDAPAttributeWizardPage();
        ldapFieldsPage = new LDAPFieldWizardPage();

        setWindowTitle("New Source");
    }

    public boolean canFinish() {

        if (!propertyPage.isPageComplete()) return false;
        if (!connectionPage.isPageComplete()) return false;

        ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();
        if (connectionConfig == null) return false;

        String adapterName = connectionConfig.getAdapterName();

        if ("JDBC".equals(adapterName)) {
            if (!jdbcTablePage.isPageComplete()) return false;
            if (!jdbcFieldsPage.isPageComplete()) return false;
            if (!jdbcPrimaryKeyPage.isPageComplete()) return false;

        } else if ("LDAP".equals(adapterName)) {
            if (!ldapTreePage.isPageComplete()) return false;
            if (!ldapAttributesPage.isPageComplete()) return false;
            if (!ldapFieldsPage.isPageComplete()) return false;
        }

        return true;
    }

    public void addPages() {
        addPage(propertyPage);
        addPage(connectionPage);

        addPage(jdbcTablePage);
        addPage(jdbcFieldsPage);
        addPage(jdbcPrimaryKeyPage);

        addPage(ldapTreePage);
        addPage(ldapAttributesPage);
        addPage(ldapFieldsPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();
            if (connectionConfig == null) return null;

            String adapterName = connectionConfig.getAdapterName();

            if ("JDBC".equals(adapterName)) {
                jdbcTablePage.setConnectionConfig(connectionConfig);
                return jdbcTablePage;

            } else if ("LDAP".equals(adapterName)) {
                ldapTreePage.setConnectionConfig(partition, connectionConfig);
                return ldapTreePage;

            } else {
                return null;
            }

        } else if (jdbcTablePage == page) {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();
            TableConfig tableConfig = jdbcTablePage.getTableConfig();
            jdbcFieldsPage.setTableConfig(connectionConfig, tableConfig);

        } else if (jdbcFieldsPage == page) {
            Collection selectedFields = jdbcFieldsPage.getSelectedFieldConfigs();
            jdbcPrimaryKeyPage.setFieldConfigs(selectedFields);

        } else if (jdbcPrimaryKeyPage == page) {
            return null;

        } else if (ldapTreePage == page) {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();
            ldapAttributesPage.setConnectionConfig(partition, connectionConfig);

        } else if (ldapAttributesPage == page) {
            Collection attributeTypes = ldapAttributesPage.getAttributeTypes();
            ldapFieldsPage.setAttributeTypes(attributeTypes);
        }

        return super.getNextPage(page);
    }

    public IWizardPage getPreviousPage(IWizardPage page) {
        if (ldapTreePage == page) {
            return connectionPage;
        }

        return super.getPreviousPage(page);
    }

    public boolean performFinish() {
        try {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();
            if (connectionConfig == null) return false;

            sourceConfig = new SourceConfig();
            sourceConfig.setName(propertyPage.getSourceName());
            sourceConfig.setConnectionName(connectionConfig.getName());

            String adapterName = connectionConfig.getAdapterName();
            if ("JDBC".equals(adapterName)) {
                TableConfig tableConfig = jdbcTablePage.getTableConfig();

                String catalog = tableConfig.getCatalog();
                String schema = tableConfig.getSchema();
                String tableName = tableConfig.getName();

                sourceConfig.setParameter(JDBCAdapter.CATALOG, catalog);
                sourceConfig.setParameter(JDBCAdapter.SCHEMA, schema);
                sourceConfig.setParameter(JDBCAdapter.TABLE, tableName);

                String filter = jdbcFieldsPage.getFilter();
                if (filter != null) {
                    sourceConfig.setParameter(JDBCAdapter.FILTER, filter);
                }

                Collection fields = jdbcPrimaryKeyPage.getFields();
                if (fields.isEmpty()) {
                    fields = jdbcFieldsPage.getSelectedFieldConfigs();
                }

                for (Iterator i=fields.iterator(); i.hasNext(); ) {
                    FieldConfig field = (FieldConfig)i.next();
                    sourceConfig.addFieldConfig(field);
                }

            } else if ("LDAP".equals(adapterName)) {
                sourceConfig.setParameter("baseDn", ldapTreePage.getBaseDn());
                sourceConfig.setParameter("filter", ldapTreePage.getFilter());
                sourceConfig.setParameter("scope", ldapTreePage.getScope());
                sourceConfig.setParameter("objectClasses", ldapTreePage.getObjectClasses());

                Collection fields = ldapFieldsPage.getFields();
                for (Iterator i=fields.iterator(); i.hasNext(); ) {
                    FieldConfig field = (FieldConfig)i.next();
                    sourceConfig.addFieldConfig(field);
                }

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

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
