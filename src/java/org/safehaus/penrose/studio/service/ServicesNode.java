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
package org.safehaus.penrose.studio.service;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.service.ServiceManagerClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.node.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.service.action.NewServiceAction;
import org.safehaus.penrose.studio.tree.Node;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class ServicesNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ServersView view;
    private ServerNode projectNode;

    public ServicesNode(String name, Node parent) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, parent);
        
        projectNode = (ServerNode)parent;
        view = projectNode.getServersView();
    }

    public void showMenu(IMenuManager manager) {
        manager.add(new NewServiceAction());
    }

    public boolean hasChildren() throws Exception {
        return !getChildren().isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        Server project = projectNode.getServer();
        PenroseClient client = project.getClient();

        ServiceManagerClient serviceManagerClient = client.getServiceManagerClient();

        for (String name : serviceManagerClient.getServiceNames()) {

            ServiceNode serviceNode = new ServiceNode(
                    name,
                    PenroseStudio.getImage(PenroseImage.SERVICE),
                    null,
                    this
            );

            children.add(serviceNode);
        }

        return children;
    }

    public ServersView getView() {
        return view;
    }

    public void setView(ServersView view) {
        this.view = view;
    }

    public ServerNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ServerNode projectNode) {
        this.projectNode = projectNode;
    }
}
