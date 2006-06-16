/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.studio.project.ProjectDialog;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.apache.log4j.Logger;

public class OpenAction extends Action {

    Logger log = Logger.getLogger(getClass());

	public OpenAction() {
        setText("&Connect...");
        setImageDescriptor(PenrosePlugin.getImageDescriptor(PenroseImage.CONNECT));
        setAccelerator(SWT.CTRL | 'O');
        setToolTipText("Connect to Penrose Server");
        setId(getClass().getName());
	}
	
	public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        PenroseApplication penroseApplication = PenroseApplication.getInstance();

        try {
            ProjectDialog dialog = new ProjectDialog(window.getShell(), SWT.NONE);
            dialog.open();

            if (dialog.getAction() == ProjectDialog.CANCEL) return;

            Project project = dialog.getProject();
            window.getShell().setText("Penrose Studio - "+project.getName());

            penroseApplication.getApplicationConfig().setCurrentProject(project);
            penroseApplication.connect(project);
            penroseApplication.open(penroseApplication.getWorkDir());
            penroseApplication.disconnect();

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            Shell shell = window.getShell();

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    "Failed opening "+penroseApplication.getApplicationConfig().getCurrentProject().getName()+" configuration.\n"+
                            "See penrose-studio-log.txt for details."
            );
        }
	}
	
}