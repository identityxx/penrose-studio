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
package org.safehaus.penrose.studio.server;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class ServersContentProvider implements ITreeContentProvider {

    protected Logger log = Logger.getLogger(getClass());

    ServersView serversView;
    Map<String,Node> servers = new TreeMap<String,Node>();

    public ServersContentProvider(ServersView serversView) {
        this.serversView = serversView;
    }

    public void init() throws Exception {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.load();

        for (ServerConfig projectConfig : penroseStudio.getApplicationConfig().getServerConfigs()) {
            addServerConfig(projectConfig);
        }
    }

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    }

    public void dispose() {
    }

    public Object[] getChildren(Object element) {
        Collection<Object> list = new ArrayList<Object>();
        try {
            Node node = (Node)element;
            
            Collection<Node> children = node.getChildren();
            if (children != null) list.addAll(children);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        return list.toArray();
    }

    public Object[] getElements(Object inputElement) {
        return servers.values().toArray();
    }

    public Object getParent(Object element) {
        Node node = (Node)element;
        return node.getParent();
    }

    public boolean hasChildren(Object element) {
        try {
            Node node = (Node)element;
            return node.hasChildren();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void addServerConfig(ServerConfig serverConfig) {
        ServerNode serverNode = new ServerNode(
                serversView,
                serverConfig.getName(),
                PenroseStudio.getImage(PenroseImage.LOGO),
                serverConfig
        );

        servers.put(serverConfig.getName(), serverNode);
    }

    public void removeProjectConfig(String name) {
        servers.remove(name);
    }
}
