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
package org.safehaus.penrose.studio.properties.tree;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.properties.editor.SystemPropertiesEditorInput;
import org.safehaus.penrose.studio.properties.editor.SystemPropertiesEditor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class SystemPropertiesNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServerNode serverNode;

    public SystemPropertiesNode(String name, Node parent) {
        super(name, PenroseStudio.getImage(PenroseImage.SYSTEM_PROPERTIES), null, parent);

        serverNode = (ServerNode)parent;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });
    }

    public void open() throws Exception {
        Server server = serverNode.getServer();

        SystemPropertiesEditorInput ei = new SystemPropertiesEditorInput();
        ei.setServer(server);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, SystemPropertiesEditor.class.getName());
    }

    public boolean hasChildren() {
        return false;
    }
}
