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
package org.safehaus.penrose.studio.partition;

import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.partition.action.NewPartitionAction;
import org.safehaus.penrose.studio.partition.action.ImportPartitionAction;
import org.safehaus.penrose.studio.partition.action.NewLDAPSnapshotPartitionAction;
import org.safehaus.penrose.studio.partition.action.NewLDAPProxyPartitionAction;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionManager;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.config.PenroseConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class PartitionsNode extends Node {

    Logger log = Logger.getLogger(getClass());

    Server server;

    public PartitionsNode(Server server, String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
        this.server = server;
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(new NewPartitionAction());
        manager.add(new ImportPartitionAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new NewLDAPSnapshotPartitionAction());
        manager.add(new NewLDAPProxyPartitionAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getPasteAction());
    }

    public boolean canPaste(Object object) throws Exception {
        return object instanceof Partition;
    }

    public void paste(Object object) throws Exception {

        Partition newPartition = (Partition)object;
        String originalName = newPartition.getName();
        PartitionManager partitionManager = server.getPartitionManager();

        int counter = 1;
        String name = originalName;
        while (partitionManager.getPartition(name) != null) {
            counter++;
            name = newPartition.getName()+" ("+counter+")";
        }

        PartitionConfig partitionConfig = newPartition.getPartitionConfig();
        partitionConfig.setName(name);
        partitionConfig.setPath("partitions/"+name);

        partitionManager.addPartition(newPartition);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();
    }

    public boolean hasChildren() throws Exception {
        PartitionManager partitionManager = server.getPartitionManager();
        return !partitionManager.getAllPartitions().isEmpty();
/*
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        return !partitionManagerClient.getPartitionNames().isEmpty();
*/
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        PartitionManager partitionManager = server.getPartitionManager();
/*
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

        for (Iterator i=partitionManagerClient.getPartitionNames().iterator(); i.hasNext(); ) {
            String partitionName = (String)i.next();
            PartitionConfig partitionConfig = partitionManagerClient.getPartitionConfig(partitionName);
*/
        for (Iterator i=partitionManager.getAllPartitions().iterator(); i.hasNext(); ) {
            Partition partition = (Partition)i.next();

            PartitionNode partitionNode = new PartitionNode(
                    server,
                    partition.getName(),
                    PenrosePlugin.getImage(PenroseImage.PARTITION),
                    partition,
                    this
            );

            partitionNode.setPartition(partition);

            children.add(partitionNode);
        }

        return children;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
