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
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.partition.PartitionConfigs;
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

            PartitionConfig partitionConfig = new PartitionConfig(name);

            PenroseApplication penroseApplication = PenroseApplication.getInstance();

            PartitionConfigs partitionConfigs = penroseApplication.getPartitionConfigs();
            partitionConfigs.addPartitionConfig(partitionConfig);

            penroseApplication.notifyChangeListeners();

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
