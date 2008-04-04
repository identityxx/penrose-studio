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
package org.safehaus.penrose.studio.partition.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.ldap.connection.LDAPConnectionWizardPage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.jndi.connection.JNDIConnectionParametersWizardPage;
import org.safehaus.penrose.studio.util.SnapshotUtil;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.ldap.LDAPClient;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.apache.log4j.Logger;

import javax.naming.Context;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class CreateLDAPSnapshotWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;

    public PartitionNamePage namePage = new PartitionNamePage();
    public LDAPConnectionWizardPage connectionInfoPage = new LDAPConnectionWizardPage();
    public JNDIConnectionParametersWizardPage connectionParametersPage = new JNDIConnectionParametersWizardPage();

    public CreateLDAPSnapshotWizard() {
        Map<String,String> parameters = new TreeMap<String,String>();
        parameters.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        connectionParametersPage.setParameters(parameters);
        setWindowTitle("Create LDAP Snapshot");
    }

    public boolean canFinish() {
        if (!namePage.isPageComplete()) return false;
        if (!connectionInfoPage.isPageComplete()) return false;
        if (!connectionParametersPage.isPageComplete()) return false;
        return true;
    }

    public void addPages() {
        addPage(namePage);
        addPage(connectionInfoPage);
        addPage(connectionParametersPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public boolean performFinish() {
        LDAPClient client = null;
        try {
            String name = namePage.getPartitionName();

            PartitionConfig partitionConfig = new PartitionConfig(name);

            PenroseStudio penroseStudio = PenroseStudio.getInstance();

            ConnectionConfig connectionConfig = new ConnectionConfig();
            connectionConfig.setName(name);
            connectionConfig.setAdapterName("LDAP");
            connectionConfig.setParameter(Context.PROVIDER_URL, connectionInfoPage.getProviderUrl());
            connectionConfig.setParameter(Context.SECURITY_PRINCIPAL, connectionInfoPage.getBindDN());
            connectionConfig.setParameter(Context.SECURITY_CREDENTIALS, connectionInfoPage.getPassword());

            Map<String,String> parameters = connectionParametersPage.getParameters();
            for (String paramName : parameters.keySet()) {
                String paramValue = parameters.get(paramName);

                connectionConfig.setParameter(paramName, paramValue);
            }

            partitionConfig.getConnectionConfigManager().addConnectionConfig(connectionConfig);

            client = new LDAPClient(connectionConfig.getParameters());

            SnapshotUtil snapshotUtil = new SnapshotUtil(project);
            snapshotUtil.createSnapshot(partitionConfig.getName(), client);

            //project.save(partitionConfig);
            
            penroseStudio.notifyChangeListeners();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);

        } finally {
            if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
