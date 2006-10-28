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
import org.safehaus.penrose.jdbc.JDBCAdapter;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.studio.driver.Driver;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.partition.Partition;
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

    public LDAPConnectionInfoWizardPage ldapInfoPage = new LDAPConnectionInfoWizardPage();
    public LDAPConnectionParametersWizardPage ldapParametersPage = new LDAPConnectionParametersWizardPage();

    public ConnectionWizard(Partition partition) {
        this.partition = partition;

        Map parameters = new TreeMap();
        parameters.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        ldapParametersPage.setParameters(parameters);

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
            if (!ldapInfoPage.isPageComplete()) return false;
            if (!ldapParametersPage.isPageComplete()) return false;
        }

        return true;
    }

    public void addPages() {
        addPage(namePage);
        addPage(driverPage);
        addPage(jdbcPage);
        //addPage(jndiPage);
        addPage(ldapInfoPage);
        addPage(ldapParametersPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (page == driverPage) {

            String adapter = driverPage.getDriver().getAdapterName();
            if ("JDBC".equals(adapter)) {
                return jdbcPage;

            } else if ("LDAP".equals(adapter)) {
                //return jndiPage;
                return ldapInfoPage;
            }

        } else if (page == jdbcPage) {
            return null;

        } else if (page == ldapInfoPage) {
            return ldapParametersPage;

        } else if (page == ldapParametersPage) {
            return null;
        }

        return super.getNextPage(page);
    }

    public IWizardPage getPreviousPage(IWizardPage page) {
        if (page == jdbcPage) {
            return driverPage;

        } else if (page == ldapInfoPage) {
            return driverPage;

        } else if (page == ldapParametersPage) {
            return ldapInfoPage;
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

                String driver = jdbcPage.getParameter(JDBCAdapter.DRIVER);
                connectionConfig.setParameter(JDBCAdapter.DRIVER, driver);

                String url = jdbcPage.getParameter(JDBCAdapter.URL);
                url = Helper.replace(url, jdbcPage.getParameters());
                connectionConfig.setParameter(JDBCAdapter.URL, url);

                String user = jdbcPage.getParameter(JDBCAdapter.USER);
                if (user != null) connectionConfig.setParameter(JDBCAdapter.USER, user);

                String password = jdbcPage.getParameter(JDBCAdapter.PASSWORD);
                if (password != null) connectionConfig.setParameter(JDBCAdapter.PASSWORD, password);

            } else if ("LDAP".equals(adapterName)) {
                connectionConfig.setParameter(InitialContext.PROVIDER_URL, ldapInfoPage.getURL()+"/"+ldapInfoPage.getSuffix());
                connectionConfig.setParameter(InitialContext.SECURITY_PRINCIPAL, ldapInfoPage.getBindDN());
                connectionConfig.setParameter(InitialContext.SECURITY_CREDENTIALS, ldapInfoPage.getPassword());

                Map parameters = ldapParametersPage.getParameters();
                for (Iterator i=parameters.keySet().iterator(); i.hasNext(); ) {
                    String paramName = (String)i.next();
                    String paramValue = (String)parameters.get(paramName);

                    connectionConfig.setParameter(paramName, paramValue);
                }
            }

            partition.addConnectionConfig(connectionConfig);

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
