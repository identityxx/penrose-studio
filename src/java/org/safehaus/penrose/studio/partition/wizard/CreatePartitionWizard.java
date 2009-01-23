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

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

/**
 * @author Endi S. Dewata
 */
public class CreatePartitionWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;

    public PartitionNamePage propertiesPage;
    public PartitionStartupPage startupPage;

    protected PartitionConfig partitionConfig;

    public CreatePartitionWizard() {
        setWindowTitle("New Partition");
    }

    public void addPages() {

        propertiesPage = new PartitionNamePage();
        propertiesPage.setPartitionName(partitionConfig.getName());

        addPage(propertiesPage);

        startupPage = new PartitionStartupPage();
        startupPage.setEnabled(partitionConfig.isEnabled());

        addPage(startupPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public boolean canFinish() {

        if (!propertiesPage.isPageComplete()) return false;
        if (!startupPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            String partitionName = propertiesPage.getPartitionName();

            partitionConfig.setName(partitionName);

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            partitionManagerClient.addPartition(partitionConfig);

            if (startupPage.isEnabled()) {
                partitionManagerClient.startPartition(partitionName);
            }

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }
}
