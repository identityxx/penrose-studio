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
package org.safehaus.penrose.studio.nis.connection.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.connection.wizard.ConnectionNamePage;
import org.safehaus.penrose.studio.config.wizard.ParametersWizardPage;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @author Endi S. Dewata
 */
public class NISConnectionWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private String partitionName;
    private ConnectionConfig connectionConfig;

    public ConnectionNamePage namePage;

    public NISConnectionSettingsWizardPage settingsPage;
    public ParametersWizardPage parametersPage;

    public NISConnectionWizard() {
        setWindowTitle("New NIS Connection");
    }

    public void addPages() {

        namePage = new ConnectionNamePage();
        addPage(namePage);

        settingsPage = new NISConnectionSettingsWizardPage();
        addPage(settingsPage);

        parametersPage = new ParametersWizardPage();
        addPage(parametersPage);
    }

    public boolean canFinish() {
        if (!namePage.isPageComplete()) return false;
        if (!settingsPage.isPageComplete()) return false;
        if (!parametersPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            connectionConfig = new ConnectionConfig();
            connectionConfig.setName(namePage.getConnectionName());
            connectionConfig.setAdapterName("NIS");

            Map<String,String> parameters = settingsPage.getParameters();
            parameters.putAll(parametersPage.getParameters());
            connectionConfig.setParameters(parameters);

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
            connectionManagerClient.createConnection(connectionConfig);
            partitionClient.store();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
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