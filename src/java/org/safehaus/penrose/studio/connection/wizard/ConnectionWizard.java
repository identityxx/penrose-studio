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
package org.safehaus.penrose.studio.connection.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionConfigs;
import org.safehaus.penrose.studio.driver.Driver;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.studio.jdbc.connection.JDBCConnectionWizardPage;
import org.safehaus.penrose.studio.jndi.connection.JNDIConnectionParametersWizardPage;
import org.safehaus.penrose.studio.jndi.connection.JNDIConnectionInfoWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.apache.log4j.Logger;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class ConnectionWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private PartitionConfig partitionConfig;
    private ConnectionConfig connectionConfig;

    public ConnectionNamePage namePage = new ConnectionNamePage();
    public ConnectionDriverPage driverPage = new ConnectionDriverPage();
    public JDBCConnectionWizardPage jdbcPage = new JDBCConnectionWizardPage();

    public JNDIConnectionInfoWizardPage jndiInfoPage = new JNDIConnectionInfoWizardPage();
    public JNDIConnectionParametersWizardPage jndiParametersPage = new JNDIConnectionParametersWizardPage();

    public ConnectionWizard(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
        setWindowTitle("New Connection");

        Map<String,String> parameters = new TreeMap<String,String>();
        parameters.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        jndiParametersPage.setParameters(parameters);

    }

    public boolean canFinish() {
        if (!namePage.isPageComplete()) return false;
        if (!driverPage.isPageComplete()) return false;

        String adapterName = driverPage.getDriver().getAdapterName();

        if ("JDBC".equals(adapterName)) {
            if (!jdbcPage.isPageComplete()) return false;

        } else if ("LDAP".equals(adapterName)) {
            //if (!jndiPage.isPageComplete()) return false;
            if (!jndiInfoPage.isPageComplete()) return false;
            if (!jndiParametersPage.isPageComplete()) return false;
        }

        return true;
    }

    public void addPages() {
        addPage(namePage);
        addPage(driverPage);
        addPage(jdbcPage);
        //addPage(jndiPage);
        addPage(jndiInfoPage);
        addPage(jndiParametersPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (page == driverPage) {

            Driver driver = driverPage.getDriver();
            String adapter = driver.getAdapterName();
            if ("JDBC".equals(adapter)) {
                jdbcPage.setDriver(driver);
                return jdbcPage;

            } else if ("LDAP".equals(adapter)) {
                //return jndiPage;
                return jndiInfoPage;
            }

        } else if (page == jdbcPage) {
            return null;

        } else if (page == jndiInfoPage) {
            return jndiParametersPage;

        } else if (page == jndiParametersPage) {
            return null;
        }

        return super.getNextPage(page);
    }

    public IWizardPage getPreviousPage(IWizardPage page) {
        if (page == jdbcPage) {
            return driverPage;

        } else if (page == jndiInfoPage) {
            return driverPage;

        } else if (page == jndiParametersPage) {
            return jndiInfoPage;
        }

        return super.getPreviousPage(page);
    }

    public boolean performFinish() {
        try {
            connectionConfig = new ConnectionConfig();
            connectionConfig.setName(namePage.getConnectionName());

            Driver type = driverPage.getDriver();
            String adapterName = type.getAdapterName();
            connectionConfig.setAdapterName(adapterName);

            if ("JDBC".equals(adapterName)) {

                Map<String,String> allParameters = jdbcPage.getAllParameters();
                String url = allParameters.get(JDBCClient.URL);
                url = Helper.replace(url, allParameters);

                Map<String,String> parameters = jdbcPage.getParameters();
                parameters.put(JDBCClient.URL, url);
                connectionConfig.setParameters(parameters);

            } else if ("LDAP".equals(adapterName)) {
                Map<String,String> parameters = jndiInfoPage.getParameters();
                parameters.putAll(jndiParametersPage.getParameters());
                connectionConfig.setParameters(parameters);
            }

            ConnectionConfigs connectionConfigs = partitionConfig.getConnectionConfigs();
            connectionConfigs.addConnectionConfig(connectionConfig);

            project.save(partitionConfig, connectionConfigs);

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
