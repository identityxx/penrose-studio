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

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.service.editor.ServiceEditorInput;
import org.safehaus.penrose.studio.service.editor.ServiceEditor;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.service.ServiceConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class ServiceNode extends Node {

    Logger log = Logger.getLogger(getClass());

    Server server;

    private ServiceConfig serviceConfig;

    public ServiceNode(String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(actions.getOpenAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getCopyAction());
        manager.add(actions.getPasteAction());
        manager.add(actions.getDeleteAction());
    }

    public void open() throws Exception {

        ServiceEditorInput ei = new ServiceEditorInput(serviceConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, ServiceEditor.class.getName());
    }

    public Object copy() throws Exception {
        return serviceConfig;
    }

    public boolean canPaste(Object object) throws Exception {
        return getParent().canPaste(object);
    }

    public void paste(Object object) throws Exception {
        getParent().paste(object);
    }

    public void delete() throws Exception {
        PenroseConfig penroseConfig = server.getPenroseConfig();
        penroseConfig.removeServiceConfig(serviceConfig.getName());
    }

    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
