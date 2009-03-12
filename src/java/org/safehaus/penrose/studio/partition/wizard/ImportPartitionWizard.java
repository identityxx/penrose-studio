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
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.Server;

import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class ImportPartitionWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Server server;

    public PartitionNamePage namePage;
    public PartitionLocationPage locationPage;
    public PartitionStartupPage startupPage;

    public ImportPartitionWizard() {
        setWindowTitle("Import Partition");
    }

    public void addPages() {

        namePage = new PartitionNamePage();

        addPage(namePage);

        locationPage = new PartitionLocationPage();
        locationPage.setDescription("Enter the location from which the partition will be imported.");

        addPage(locationPage);

        startupPage = new PartitionStartupPage();

        addPage(startupPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public boolean canFinish() {

        if (!namePage.isPageComplete()) return false;
        if (!locationPage.isPageComplete()) return false;
        if (!startupPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {

            String partitionName = namePage.getPartitionName();
            String location = locationPage.getLocation();

            File dir = new File(location);
            if (!dir.isDirectory()) {
                throw new Exception(dir+" folder does not exist.");
            }

            PenroseClient client = server.getClient();

            client.uploadFolder(dir, "partitions/"+partitionName);

            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            partitionManagerClient.loadPartition(partitionName);

            if (startupPage.isEnabled()) {
                partitionManagerClient.startPartition(partitionName);
            }

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
}
