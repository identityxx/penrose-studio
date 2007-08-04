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
package org.safehaus.penrose.studio.connection;

import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.connection.action.NewConnectionAction;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class ConnectionsNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    private PartitionConfig partitionConfig;

    public ConnectionsNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new NewConnectionAction(this));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

    }

    public void paste() throws Exception {

        Object newObject = view.getClipboard();

        if (!(newObject instanceof ConnectionConfig)) return;

        ConnectionConfig newConnectionConfig = (ConnectionConfig)((ConnectionConfig)newObject).clone();

        int counter = 1;
        String name = newConnectionConfig.getName();
        while (partitionConfig.getConnectionConfigs().getConnectionConfig(name) != null) {
            counter++;
            name = newConnectionConfig.getName()+" ("+counter+")";
        }

        newConnectionConfig.setName(name);
        partitionConfig.getConnectionConfigs().addConnectionConfig(newConnectionConfig);

        view.setClipboard(null);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        return !partitionConfig.getConnectionConfigs().getConnectionConfigs().isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {
        
        Collection<Node> children = new ArrayList<Node>();

        Collection connectionConfigs = partitionConfig.getConnectionConfigs().getConnectionConfigs();
        for (Iterator i=connectionConfigs.iterator(); i.hasNext(); ) {
            ConnectionConfig connectionConfig = (ConnectionConfig)i.next();

            ConnectionNode connectionNode = new ConnectionNode(
                    view,
                    connectionConfig.getName(),
                    ObjectsView.CONNECTION,
                    PenrosePlugin.getImage(PenroseImage.CONNECTION),
                    connectionConfig,
                    this
            );

            connectionNode.setPartitionConfig(this.partitionConfig);
            connectionNode.setConnectionConfig(connectionConfig);

            children.add(connectionNode);
        }

        return children;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }
}
