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
import org.safehaus.penrose.partition.*;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class ExportPartitionWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private Partition partition;

    public PartitionLocationPage locationPage = new PartitionLocationPage();

    public ExportPartitionWizard(Partition partition) {
        this.partition = partition;

        setWindowTitle("Export Partition");
        locationPage.setDescription("Enter the location to which the partition will be exported.");
    }

    public boolean canFinish() {

        if (!locationPage.isPageComplete()) return false;

        return true;
    }

    public boolean performFinish() {
        try {

            String directory = locationPage.getLocation();

            PartitionWriter partitionWriter = new PartitionWriter(directory);
            partitionWriter.write(partition);

            return true;

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return false;
        }
    }

    public void addPages() {
        addPage(locationPage);
    }

    public boolean needsPreviousAndNextButtons() {
        return true;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
