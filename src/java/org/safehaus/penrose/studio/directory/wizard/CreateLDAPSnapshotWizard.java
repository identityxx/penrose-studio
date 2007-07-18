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
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.ldap.LDAPClient;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class CreateLDAPSnapshotWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    Partition partition;
    SelectConnectionWizardPage connectionPage;

    public CreateLDAPSnapshotWizard(Partition partition) {
        this.partition = partition;
        this.connectionPage = new SelectConnectionWizardPage(partition, "LDAP");
        setWindowTitle("Create LDAP Snapshot");
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();

            LDAPClient client = new LDAPClient(connectionConfig.getParameters());

            SnapshotUtil snapshotUtil = new SnapshotUtil();
            snapshotUtil.createSnapshot(partition, client);
            
            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return false;
        }
    }

    public void addPages() {
        addPage(connectionPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }
}
