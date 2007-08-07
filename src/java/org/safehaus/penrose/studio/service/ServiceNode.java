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
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.service.ServiceConfigs;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
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

    ObjectsView view;

    private ServiceConfig serviceConfig;

    public ServiceNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
        serviceConfig = (ServiceConfig)object;
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

        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.DELETE)) {
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

        ServiceEditorInput ei = new ServiceEditorInput(serviceConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, ServiceEditor.class.getName());
    }

    public void load() throws Exception {
        log.debug("Opening "+name+" service.");

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        File workDir = penroseStudio.getWorkDir();
        File dir = new File(workDir, "services"+File.separator+name);

        penroseStudio.downloadFolder("services"+File.separator+name, workDir);

        ServiceConfigs serviceConfigs = penroseStudio.getServiceConfigs();
        serviceConfig = serviceConfigs.load(dir);
    }

    public void save() throws Exception {
        log.debug("Saving "+name+" service.");

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        ServiceConfigs serviceConfigs = penroseStudio.getServiceConfigs();

        File workDir = penroseStudio.getWorkDir();
        //serviceConfigs.store(workDir, serviceConfig);
    }

    public void remove() throws Exception {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        boolean confirm = MessageDialog.openQuestion(
                shell,
                "Confirmation",
                "Remove \""+serviceConfig.getName()+"\"?");

        if (!confirm) return;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        ServiceConfigs serviceConfigs = penroseStudio.getServiceConfigs();
        serviceConfigs.removeServiceConfig(serviceConfig.getName());

        serviceConfig = null;

        penroseStudio.notifyChangeListeners();
    }

    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }
}
