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
package org.safehaus.penrose.studio.ldap.source.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.source.wizard.SourceWizard;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.ldap.LDAP;

/**
 * @author Endi S. Dewata
 */
public class LDAPSourcePropertiesWizard extends SourceWizard {

    public SelectConnectionWizardPage connectionPage;
    public LDAPSourceTreeWizardPage treePage;

    public LDAPSourcePropertiesWizard() throws Exception {
        setWindowTitle("Edit LDAP Source Properties");
    }

    public void addPages() {
        try {
            connectionPage = new SelectConnectionWizardPage();

            connectionPage.setServer(server);
            connectionPage.setPartitionName(partitionName);
            connectionPage.setAdapterType("LDAP");
            connectionPage.setConnectionName(sourceConfig.getConnectionName());

            addPage(connectionPage);

            treePage = new LDAPSourceTreeWizardPage();
            treePage.setServer(server);
            treePage.setPartitionName(partitionName);
            treePage.setConnectionName(sourceConfig.getConnectionName());

            treePage.setBaseDn(sourceConfig.getParameter(LDAP.BASE_DN));
            treePage.setFilter(sourceConfig.getParameter(LDAP.FILTER));
            treePage.setScope(sourceConfig.getParameter(LDAP.SCOPE));
            treePage.setObjectClasses(sourceConfig.getParameter(LDAP.OBJECT_CLASSES));

            treePage.init();

            addPage(treePage);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        if (!treePage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            connectionConfig = connectionPage.getConnectionConfig();
            if (connectionConfig == null) return null;
            treePage.setConnectionName(connectionPage.getConnectionName());
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            sourceConfig.setConnectionName(connectionConfig.getName());

            sourceConfig.setParameter(LDAP.BASE_DN, treePage.getBaseDn());
            sourceConfig.setParameter(LDAP.FILTER, treePage.getFilter());
            sourceConfig.setParameter(LDAP.SCOPE, treePage.getScope());
            sourceConfig.setParameter(LDAP.OBJECT_CLASSES, treePage.getObjectClasses());

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