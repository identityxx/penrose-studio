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
package org.safehaus.penrose.studio.engine;

import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.engine.EngineConfig;
import org.safehaus.penrose.config.PenroseConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class EnginesNode extends Node {

    Server server;

    public EnginesNode(Server server, String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
        this.server = server;
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(actions.getPasteAction());
    }

    public boolean canPaste(Object object) throws Exception {
        return object instanceof EngineConfig;
    }

    public void paste(Object object) throws Exception {
        EngineConfig engineConfig = (EngineConfig)object;
        PenroseConfig penroseConfig = server.getPenroseConfig();

        int counter = 1;
        String name = engineConfig.getName();

        while (penroseConfig.getEngineConfig(name) != null) {
            counter++;
            name = engineConfig.getName()+" ("+counter+")";
        }

        engineConfig.setName(name);
        penroseConfig.addEngineConfig(engineConfig);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        ServerNode serverNode = (ServerNode)getParent();
        Server server = serverNode.getServer();
        Collection engineConfigs = server.getPenroseConfig().getEngineConfigs();

        for (Iterator i=engineConfigs.iterator(); i.hasNext(); ) {
            EngineConfig engineConfig = (EngineConfig)i.next();

            children.add(new EngineNode(
                    server,
                    engineConfig.getName(),
                    PenrosePlugin.getImage(PenroseImage.ENGINE),
                    engineConfig,
                    this
            ));
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
