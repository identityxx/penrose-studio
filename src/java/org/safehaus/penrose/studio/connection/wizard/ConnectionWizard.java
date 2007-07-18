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
import org.safehaus.penrose.studio.driver.Driver;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.apache.log4j.Logger;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Map;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class ConnectionWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Partition partition;
    private ConnectionConfig connectionConfig;

    public ConnectionNamePage namePage = new ConnectionNamePage();
    public ConnectionDriverPage driverPage = new ConnectionDriverPage();
    public JDBCConnectionWizardPage jdbcPage = new JDBCConnectionWizardPage();

    public JNDIConnectionInfoWizardPage jndiInfoPage = new JNDIConnectionInfoWizardPage();
    public JNDIConnectionParametersWizardPage jndiParametersPage = new JNDIConnectionParametersWizardPage();

    public ConnectionWizard(Partition partition) {
        this.partition = partition;

        Map parameters = new TreeMap();
        parameters.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        jndiParametersPage.setParameters(parameters);

        setWindowTitle("New Connection");
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

            String adapter = driverPage.getDriver().getAdapterName();
            if ("JDBC".equals(adapter)) {
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

                Map<String,String> parameters = jdbcPage.getParameters();
                connectionConfig.setParameters(parameters);

                Map<String,String> allParameters = jdbcPage.getAllParameters();
                String url = allParameters.get(JDBCClient.URL);
                url = Helper.replace(url, allParameters);
                connectionConfig.setParameter(JDBCClient.URL, url);

            } else if ("LDAP".equals(adapterName)) {
                connectionConfig.setParameter(InitialContext.PROVIDER_URL, jndiInfoPage.getURL()+"/"+jndiInfoPage.getSuffix());
                connectionConfig.setParameter(InitialContext.SECURITY_PRINCIPAL, jndiInfoPage.getBindDN());
                connectionConfig.setParameter(InitialContext.SECURITY_CREDENTIALS, jndiInfoPage.getPassword());

                Map parameters = jndiParametersPage.getParameters();
                for (Iterator i=parameters.keySet().iterator(); i.hasNext(); ) {
                    String paramName = (String)i.next();
                    String paramValue = (String)parameters.get(paramName);

                    connectionConfig.setParameter(paramName, paramValue);
                }
            }

            partition.getConnections().addConnectionConfig(connectionConfig);

            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
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

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
