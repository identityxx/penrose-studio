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
import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.studio.util.SnapshotUtil;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.ldap.connection.LDAPConnectionClient;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.filter.FilterTool;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class ImportEntriesWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    Server server;
    String partitionName;
    DN targetDn;
    
    SelectConnectionWizardPage connectionPage;
    ImportTreeWizardPage treePage;

    public ImportEntriesWizard() {
        setWindowTitle("Import Entries");
    }

    public void addPages() {

        connectionPage = new SelectConnectionWizardPage();
        connectionPage.setServer(server);
        connectionPage.setPartitionName(partitionName);
        connectionPage.setAdapterType("LDAP");

        addPage(connectionPage);

        treePage = new ImportTreeWizardPage();
        treePage.setServer(server);
        treePage.setPartitionName(partitionName);

        addPage(treePage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            ConnectionConfig connectionConfig = connectionPage.getConnectionConfig();
            if (connectionConfig == null) return null;
            treePage.setConnectionName(connectionPage.getConnectionName());
        }

        return super.getNextPage(page);
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        if (!treePage.isPageComplete()) return false;
        return true;
    }

    public boolean performFinish() {
        try {
            SnapshotUtil util = new SnapshotUtil();
            util.setServer(server);
            util.setPartitionName(partitionName);
            util.setConnectionName(connectionPage.getConnectionName());

            util.setSourceDn(new DN(treePage.getBaseDn()));
            util.setTargetDn(targetDn);

            String filter = treePage.getFilter();
            if (filter != null) {
                util.setFilter(FilterTool.parseFilter(filter));
            }

            util.setDepth(treePage.getDepth());

            util.run();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public DN getTargetDn() {
        return targetDn;
    }

    public void setTargetDn(DN targetDn) {
        this.targetDn = targetDn;
    }
}
