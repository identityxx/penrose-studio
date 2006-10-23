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
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.partition.action.NewPartitionAction;
import org.safehaus.penrose.studio.partition.action.ImportPartitionAction;
import org.safehaus.penrose.studio.partition.action.NewLDAPSnapshotPartitionAction;
import org.safehaus.penrose.studio.partition.action.NewLDAPProxyPartitionAction;
import org.safehaus.penrose.studio.object.ObjectsView;
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

    ObjectsView view;

    public PartitionsNode(ObjectsView view, String name, String type, Image image, Object object, Node parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {
        manager.add(new NewPartitionAction());
        manager.add(new ImportPartitionAction());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseWorkbenchAdvisor workbenchAdvisor = penroseStudio.getWorkbenchAdvisor();
        PenroseWorkbenchWindowAdvisor workbenchWindowAdvisor = workbenchAdvisor.getWorkbenchWindowAdvisor();
        PenroseActionBarAdvisor actionBarAdvisor = workbenchWindowAdvisor.getActionBarAdvisor();

        //if (actionBarAdvisor.getShowCommercialFeaturesAction().isChecked()) {
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            manager.add(new NewLDAPSnapshotPartitionAction());
            manager.add(new NewLDAPProxyPartitionAction());
        //}
    }

    public boolean hasChildren() throws Exception {
        ServerNode serverNode = (ServerNode)getParent();
        Server server = serverNode.getProject();
        PenroseConfig penroseConfig = server.getPenroseConfig();
        return !penroseConfig.getPartitionConfigs().isEmpty();
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        ServerNode serverNode = (ServerNode)getParent();
        Server server = serverNode.getProject();

        PenroseConfig penroseConfig = server.getPenroseConfig();
        PartitionManager partitionManager = server.getPartitionManager();

        Collection partitionConfigs = penroseConfig.getPartitionConfigs();
        for (Iterator i=partitionConfigs.iterator(); i.hasNext(); ) {
            PartitionConfig partitionConfig = (PartitionConfig)i.next();
            Partition partition = partitionManager.getPartition(partitionConfig.getName());

            PartitionNode partitionNode = new PartitionNode(
                    view,
                    server,
                    partitionConfig.getName(),
                    ObjectsView.PARTITION,
                    PenrosePlugin.getImage(PenroseImage.PARTITION),
                    partitionConfig,
                    this
            );

            partitionNode.setPartitionConfig(partitionConfig);
            partitionNode.setPartition(partition);

            children.add(partitionNode);
        }

        return children;
    }
}
