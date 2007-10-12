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
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.service.ServiceConfigs;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class ServiceNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;
    ProjectNode projectNode;
    ServicesNode servicesNode;

    private ServiceConfig serviceConfig;

    public ServiceNode(String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        serviceConfig = (ServiceConfig)object;
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

        manager.add(new Action("Delete", PenroseStudioPlugin.getImageDescriptor(PenroseImage.SIZE_16x16, PenroseImage.DELETE)) {
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
        if (serviceConfig == null) {
            load();
        }

        ServiceEditorInput ei = new ServiceEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setServiceConfig(serviceConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, ServiceEditor.class.getName());
    }

    public void load() throws Exception {
        log.debug("Opening "+name+" service.");

        Project project = projectNode.getProject();
        project.download("services"+File.separator+name);

        ServiceConfigs serviceConfigs = project.getServiceConfigs();
        serviceConfig = serviceConfigs.load(name);
        serviceConfigs.addServiceConfig(serviceConfig);
    }

    public void save() throws Exception {
        log.debug("Saving "+name+" service.");

        Project project = projectNode.getProject();
        ServiceConfigs serviceConfigs = project.getServiceConfigs();

        File workDir = project.getWorkDir();
        //serviceConfigs.store(workDir, serviceConfig);
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Confirmation",
                "Remove \""+serviceConfig.getName()+"\"?");

        if (!confirm) return;

        Project project = projectNode.getProject();
        ServiceConfigs serviceConfigs = project.getServiceConfigs();
        serviceConfigs.removeServiceConfig(serviceConfig.getName());

        serviceConfig = null;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }
}
