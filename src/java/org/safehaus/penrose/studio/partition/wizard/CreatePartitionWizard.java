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
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.project.Project;

/**
 * @author Endi S. Dewata
 */
public class CreatePartitionWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    public Project project;
    public PartitionsNode partitionsNode;

    public PartitionNamePage namePage = new PartitionNamePage();
    public PartitionClassPage classPage = new PartitionClassPage();

    public CreatePartitionWizard(Project project, PartitionsNode partitionsNode) {
        this.project = project;
        this.partitionsNode = partitionsNode;
        setWindowTitle("New Partition");
    }

    public void addPages() {
        addPage(namePage);
        addPage(classPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public boolean canFinish() {

        if (!namePage.isPageComplete()) return false;
        if (!classPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {
            String partitionName = namePage.getPartitionName();
            String partitionClass = classPage.getPartitionClass();

            PartitionConfig partitionConfig = new PartitionConfig(partitionName);
            partitionConfig.setPartitionClass(partitionClass);

            //PartitionConfigManager partitionConfigManager = project.getPartitionConfigManager();
            //partitionConfigManager.addPartitionConfig(partitionConfig);

            //project.save(partitionConfig);
            
            //partitionsNode.addPartitionConfig(partitionConfig);

            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            partitionManagerClient.addPartition(partitionConfig);

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }
}
