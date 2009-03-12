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
package org.safehaus.penrose.studio.partition.editor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;

public class PartitionEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());

    Server server;
    String partitionName;

    PartitionClient partitionClient;
    PartitionConfig partitionConfig;

    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        PartitionEditorInput ei = (PartitionEditorInput)input;

        server = ei.getServer();
        partitionName = ei.getPartitionName();

        try {
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            partitionConfig = partitionClient.getPartitionConfig();

        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }

        setPartName(partitionName);
    }

    protected void addPages() {
        try {
            addPage(new PartitionPropertiesPage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
    }

    public void doSaveAs() {
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void store() throws Exception {

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        partitionManagerClient.updatePartition(partitionName, partitionConfig);

        setPartName(partitionName);
    }
}