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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.swt.graphics.Image;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.partition.action.ExportPartitionAction;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.connection.ConnectionsNode;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.module.ModulesNode;
import org.safehaus.penrose.studio.source.SourcesNode;
import org.safehaus.penrose.studio.directory.DirectoryNode;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionManager;
import org.safehaus.penrose.config.PenroseConfig;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class PartitionNode extends Node {

    Logger log = Logger.getLogger(getClass());

    Server server;

    private Partition partition;

    public PartitionNode(
            Server server,
            String name,
            Image image,
            Object object,
            Node parent
    ) {
        super(name, image, object, parent);
        this.server = server;
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(new ExportPartitionAction(partition));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getCopyAction());
        manager.add(actions.getPasteAction());
        manager.add(actions.getDeleteAction());
    }

    public void delete() throws Exception {
        PenroseConfig penroseConfig = server.getPenroseConfig();
        penroseConfig.removePartitionConfig(partition.getName());

        PartitionManager partitionManager = server.getPartitionManager();
        partitionManager.removePartition(partition.getName());
    }

    public Object copy() throws Exception {
        return partition;
    }

    public boolean canPaste(Object object) throws Exception {
        return getParent().canPaste(object);
    }

    public void paste(Object object) throws Exception {
        getParent().paste(object);
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        DirectoryNode directoryNode = new DirectoryNode(
                ObjectsView.DIRECTORY,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.DIRECTORY,
                this
        );

        directoryNode.setServer(server);
        directoryNode.setPartition(partition);

        children.add(directoryNode);

        ConnectionsNode connectionsNode = new ConnectionsNode(
                ObjectsView.CONNECTIONS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.CONNECTIONS,
                this
        );

        connectionsNode.setServer(server);
        connectionsNode.setPartition(partition);

        children.add(connectionsNode);

        SourcesNode sourcesNode = new SourcesNode(
                server,
                ObjectsView.SOURCES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.SOURCES,
                this
        );

        sourcesNode.setPartition(partition);

        children.add(sourcesNode);

        ModulesNode modulesNode = new ModulesNode(
                ObjectsView.MODULES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.MODULES,
                this
        );

        modulesNode.setPartition(partition);

        children.add(modulesNode);

        return children;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
