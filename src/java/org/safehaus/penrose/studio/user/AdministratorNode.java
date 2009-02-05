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
package org.safehaus.penrose.studio.user;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.node.ServerNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.user.UserConfig;

/**
 * @author Endi S. Dewata
 */
public class AdministratorNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServerNode projectNode;

    public AdministratorNode(String name, Object object, Node parent) {
        super(name, PenroseStudio.getImage(PenroseImage.ADMINISTRATOR), object, parent);
        
        projectNode = (ServerNode)parent;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {
        Server project = projectNode.getServer();
        PenroseClient penroseClient = project.getClient();
        UserConfig rootUserConfig = penroseClient.getRootUserConfig();
                
        UserEditorInput ei = new UserEditorInput();
        ei.setProject(project);
        ei.setUserConfig(rootUserConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, UserEditor.class.getName());
    }
}
