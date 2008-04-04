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
package org.safehaus.penrose.studio.project.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.util.ApplicationConfig;
import org.apache.log4j.Logger;

public class SaveAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public SaveAction() {
        setText("&Save");
        setImageDescriptor(PenroseStudioPlugin.getImageDescriptor(PenroseImage.SAVE));
        setAccelerator(SWT.CTRL | 'S');
        setToolTipText("Save changes to disk");
        setId(getClass().getName());
    }

    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        page.saveAllEditors(false);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        ApplicationConfig applicationConfig = penroseStudio.getApplicationConfig();

        try {
            ServersView serversView = ServersView.getInstance();
            ProjectNode projectNode = serversView.getSelectedProjectNode();
            if (projectNode == null) return;

            Project project = projectNode.getProject();

            //project.save();

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            Shell shell = window.getShell();

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    "Failed saving "+applicationConfig.getCurrentProject().getName()+" configuration.\n"+
                            "See penrose-studio.log for details."
            );
        }
    }
}