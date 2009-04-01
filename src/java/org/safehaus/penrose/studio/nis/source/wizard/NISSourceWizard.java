/**
 * Copyright 2009 Red Hat, Inc.
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
package org.safehaus.penrose.studio.nis.source.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.source.wizard.SourceWizard;
import org.safehaus.penrose.studio.source.wizard.SourcePropertiesWizardPage;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.nis.NIS;

/**
 * @author Endi S. Dewata
 */
public class NISSourceWizard extends SourceWizard {

    public SourcePropertiesWizardPage propertiesPage;
    public SelectConnectionWizardPage connectionPage;
    public NISSourcePropertiesWizardPage nisPropertiesPage;

    public NISSourceWizard() throws Exception {
        setWindowTitle("New NIS Source");
    }

    public void addPages() {

        propertiesPage = new SourcePropertiesWizardPage();

        propertiesPage.setSourceName(sourceConfig.getName());
        propertiesPage.setClassName(sourceConfig.getSourceClass());
        propertiesPage.setEnabled(sourceConfig.isEnabled());
        propertiesPage.setSourceDescription(sourceConfig.getDescription());

        addPage(propertiesPage);

        if (connectionConfig == null) {
            connectionPage = new SelectConnectionWizardPage();
            connectionPage.setServer(server);
            connectionPage.setPartitionName(partitionName);
            connectionPage.setAdapterType("NIS");

            addPage(connectionPage);
        }

        nisPropertiesPage = new NISSourcePropertiesWizardPage();

        addPage(nisPropertiesPage);

    }

    public boolean canFinish() {
        if (!propertiesPage.isPageComplete()) return false;
        if (connectionPage != null && !connectionPage.isPageComplete()) return false;
        if (!nisPropertiesPage.isPageComplete()) return false;

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (connectionPage == page) {
            connectionConfig = connectionPage.getConnectionConfig();
            if (connectionConfig == null) return null;
        }

        return super.getNextPage(page);
    }

    public boolean performFinish() {
        try {
            sourceConfig = new SourceConfig();
            sourceConfig.setName(propertiesPage.getSourceName());
            sourceConfig.setSourceClass(propertiesPage.getClassName());
            sourceConfig.setEnabled(propertiesPage.isEnabled());
            sourceConfig.setDescription(propertiesPage.getSourceDescription());

            sourceConfig.setConnectionName(connectionPage.getConnectionName());

            sourceConfig.setParameter(NIS.BASE, nisPropertiesPage.getBase());
            sourceConfig.setParameter(NIS.OBJECT_CLASSES, nisPropertiesPage.getObjectClasses());

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
            sourceManagerClient.createSource(sourceConfig);
            partitionClient.store();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }
}