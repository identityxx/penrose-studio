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
package org.safehaus.penrose.studio.service.tree;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.service.ServiceManagerClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.action.RefreshAction;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.service.action.NewServiceAction;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class ServicesNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ServersView view;
    private ServerNode serverNode;

    public ServicesNode(String name, Node parent) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, parent);
        serverNode = (ServerNode)parent;
        view = serverNode.getServersView();
    }

    public void update() throws Exception {

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();

        ServiceManagerClient serviceManagerClient = client.getServiceManagerClient();

        for (String name : serviceManagerClient.getServiceNames()) {

            ServiceNode serviceNode = new ServiceNode(
                    name,
                    PenroseStudio.getImage(PenroseImage.SERVICE),
                    null,
                    this
            );

            addChild(serviceNode);
        }
    }

    public void expand() throws Exception {
        if (children == null) update();
    }

    public void refresh() throws Exception {
        removeChildren();
        update();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new NewServiceAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new RefreshAction(this));
    }

    public ServersView getView() {
        return view;
    }

    public void setView(ServersView view) {
        this.view = view;
    }

    public ServerNode getServerNode() {
        return serverNode;
    }

    public void setServerNode(ServerNode serverNode) {
        this.serverNode = serverNode;
    }
}
