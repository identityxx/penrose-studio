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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.service.ServiceManagerClient;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.service.editor.ServiceEditor;
import org.safehaus.penrose.studio.service.editor.ServiceEditorInput;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.node.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class ServiceNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;
    ServerNode projectNode;
    ServicesNode servicesNode;

    public ServiceNode(String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
        servicesNode = (ServicesNode)parent;
        projectNode = servicesNode.getProjectNode();
        view = projectNode.getServersView();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Start") {
            public void run() {
                try {
                    start();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Stop") {
            public void run() {
                try {
                    stop();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Delete", PenroseStudio.getImageDescriptor(PenroseImage.DELETE_SMALL)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        log.debug("Opening "+name+" service.");

        Server project = projectNode.getServer();
        PenroseClient client = project.getClient();
        ServiceManagerClient serviceManagerClient = client.getServiceManagerClient();
        ServiceConfig serviceConfig = serviceManagerClient.getServiceConfig(name);

        ServiceEditorInput ei = new ServiceEditorInput();
        ei.setProject(projectNode.getServer());
        ei.setServiceConfig(serviceConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, ServiceEditor.class.getName());
    }

    public void start() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Confirmation",
                "Start \""+name+"\"?");

        if (!confirm) return;

        Server project = projectNode.getServer();
        PenroseClient client = project.getClient();
        ServiceManagerClient serviceManagerClient = client.getServiceManagerClient();
        serviceManagerClient.startService(name);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void stop() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Confirmation",
                "Stop \""+name+"\"?");

        if (!confirm) return;

        Server project = projectNode.getServer();
        PenroseClient client = project.getClient();
        ServiceManagerClient serviceManagerClient = client.getServiceManagerClient();
        serviceManagerClient.stopService(name);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Confirmation",
                "Remove \""+name+"\"?");

        if (!confirm) return;

        Server project = projectNode.getServer();
        PenroseClient client = project.getClient();
        ServiceManagerClient serviceManagerClient = client.getServiceManagerClient();
        serviceManagerClient.removeService(name);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }
}
