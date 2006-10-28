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

import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.service.action.NewServiceAction;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.service.ServiceConfig;
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
public class ServicesNode extends Node {

    Logger log = Logger.getLogger(getClass());

    Server server;

    public ServicesNode(String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(new NewServiceAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getPasteAction());
    }

    public boolean canPaste(Object object) throws Exception {
        return object instanceof ServiceConfig;
    }

    public void paste(Object object) throws Exception {
        ServiceConfig serviceConfig = (ServiceConfig)object;
        PenroseConfig penroseConfig = server.getPenroseConfig();

        int counter = 1;
        String name = serviceConfig.getName();

        while (penroseConfig.getServiceConfig(name) != null) {
            counter++;
            name = serviceConfig.getName()+" ("+counter+")";
        }

        serviceConfig.setName(name);
        penroseConfig.addServiceConfig(serviceConfig);
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        PenroseConfig penroseConfig = server.getPenroseConfig();
        for (Iterator i=penroseConfig.getServiceConfigs().iterator(); i.hasNext(); ) {
            ServiceConfig serviceConfig = (ServiceConfig)i.next();

            ServiceNode serviceNode = new ServiceNode(
                    serviceConfig.getName(),
                    PenrosePlugin.getImage(PenroseImage.SERVICE),
                    serviceConfig,
                    this
            );

            serviceNode.setServiceConfig(serviceConfig);
            serviceNode.setServer(server);

            children.add(serviceNode);
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
