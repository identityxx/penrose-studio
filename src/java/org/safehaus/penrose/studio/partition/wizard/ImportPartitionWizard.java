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
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class ImportPartitionWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    ProjectNode projectNode;
    public PartitionNamePage namePage = new PartitionNamePage();
    public PartitionLocationPage locationPage = new PartitionLocationPage();

    public ImportPartitionWizard(ProjectNode projectNode) {
        this.projectNode = projectNode;
        setWindowTitle("Import Partition");
        locationPage.setDescription("Enter the location from which the partition will be imported.");
    }

    public boolean canFinish() {

        if (!namePage.isPageComplete()) return false;
        if (!locationPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {

            String directory = locationPage.getLocation();

            File dir = new File(directory);
            if (!dir.isDirectory()) return false;

            Project project = projectNode.getProject();
            PartitionConfigs partitionConfigs = project.getPartitionConfigs();
            partitionConfigs.load(dir);

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public void addPages() {
        addPage(namePage);
        addPage(locationPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }
}
