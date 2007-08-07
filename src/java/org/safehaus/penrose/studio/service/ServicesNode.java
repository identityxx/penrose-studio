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

import org.safehaus.penrose.studio.service.action.NewServiceAction;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.service.ServiceConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class ServicesNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    protected Map<String,Node> children;

    public ServicesNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new NewServiceAction());

    }

    public void refresh() throws Exception {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseClient penroseClient = penroseStudio.getClient();

        children = new TreeMap<String,Node>();

        for (ServiceConfig serviceConfig : penroseStudio.getServiceConfigs().getServiceConfigs()) {

            ServiceNode serviceNode = new ServiceNode(
                    view,
                    serviceConfig.getName(),
                    ObjectsView.SERVICE,
                    PenrosePlugin.getImage(PenroseImage.SERVICE),
                    serviceConfig,
                    this
            );

            children.put(serviceConfig.getName(), serviceNode);
        }

        for (String name : penroseClient.getServiceNames()) {

            if (children.containsKey(name)) continue;

            ServiceNode serviceNode = new ServiceNode(
                    view,
                    name,
                    ObjectsView.SERVICE,
                    PenrosePlugin.getImage(PenroseImage.SERVICE),
                    null,
                    this
            );

            children.put(name, serviceNode);
        }
    }

    public boolean hasChildren() throws Exception {

        if (children == null) {
            refresh();
        }

        return !children.isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        if (children == null) {
            refresh();
        }

        return children.values();
    }
}
