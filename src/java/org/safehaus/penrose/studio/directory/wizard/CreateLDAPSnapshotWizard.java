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
package org.safehaus.penrose.studio.directory.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.studio.util.SnapshotUtil;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.ldap.LDAPClient;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class CreateLDAPSnapshotWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private String partitionName;
    SelectConnectionWizardPage connectionPage;

    public CreateLDAPSnapshotWizard() {
        setWindowTitle("Create LDAP Snapshot");

        connectionPage = new SelectConnectionWizardPage(partitionName, "LDAP");
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        LDAPClient client = null;
        try {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();

            client = new LDAPClient(connectionConfig.getParameters());

            SnapshotUtil snapshotUtil = new SnapshotUtil(project);
            snapshotUtil.createSnapshot(partitionName, client);

            //project.save(partitionConfig);
            
            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);

        } finally {
            if (client != null) try { client.close(); } catch (Exception e) { log.error(e.getMessage(), e); }
        }
    }

    public void addPages() {
        addPage(connectionPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }
}
