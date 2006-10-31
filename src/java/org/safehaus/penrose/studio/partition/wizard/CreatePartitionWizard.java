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
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.partition.PartitionManager;
import org.safehaus.penrose.config.PenroseConfig;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class CreatePartitionWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public PartitionNamePage namePage = new PartitionNamePage();

    public CreatePartitionWizard() {
        setWindowTitle("New Partition");
    }

    public boolean canFinish() {

        if (!namePage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            String name = namePage.getPartitionName();
            String path = "partitions/"+name;

            PartitionConfig partitionConfig = new PartitionConfig();
            partitionConfig.setName(name);
            partitionConfig.setPath(path);

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            ServerNode serverNode = penroseStudio.getSelectedServerNode();
            if (serverNode == null) return false;

            Server server = serverNode.getServer();

            PartitionManager partitionManager = server.getPartitionManager();
            partitionManager.load(serverNode.getWorkDir(), partitionConfig);

            penroseStudio.fireChangeEvent();

            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return false;
        }
    }

    public void addPages() {
        addPage(namePage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }
}
