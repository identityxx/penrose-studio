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

import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.source.wizard.SourceWizard;
import org.safehaus.penrose.studio.connection.wizard.SelectConnectionWizardPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.nis.NIS;

/**
 * @author Endi S. Dewata
 */
public class NISSourcePropertiesWizard extends SourceWizard {

    public SelectConnectionWizardPage connectionPage;
    public NISSourcePropertiesWizardPage nisPropertiesPage;

    public NISSourcePropertiesWizard() throws Exception {
        setWindowTitle("Edit NIS Source Properties");
    }

    public void addPages() {

        connectionPage = new SelectConnectionWizardPage();

        connectionPage.setServer(server);
        connectionPage.setPartitionName(partitionName);
        connectionPage.setAdapterType("NIS");
        connectionPage.setConnectionName(sourceConfig.getConnectionName());

        addPage(connectionPage);

        nisPropertiesPage = new NISSourcePropertiesWizardPage();

        nisPropertiesPage.setBase(sourceConfig.getParameter(NIS.BASE));
        nisPropertiesPage.setObjectClasses(sourceConfig.getParameter(NIS.OBJECT_CLASSES));

        addPage(nisPropertiesPage);

    }

    public boolean canFinish() {
        if (!connectionPage.isPageComplete()) return false;
        if (!nisPropertiesPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            sourceConfig.setConnectionName(connectionPage.getConnectionName());

            sourceConfig.setParameter(NIS.BASE, nisPropertiesPage.getBase());
            sourceConfig.setParameter(NIS.OBJECT_CLASSES, nisPropertiesPage.getObjectClasses());

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

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig connection) {
        this.sourceConfig = connection;
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}