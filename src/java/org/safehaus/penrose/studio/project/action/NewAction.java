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

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.studio.project.ProjectEditorDialog;
import org.safehaus.penrose.studio.project.ProjectConfig;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.ServersView;
import org.apache.log4j.Logger;

public class NewAction extends Action {

    Logger log = Logger.getLogger(getClass());

	public NewAction() {
        setText("&New Connection...");
        setImageDescriptor(PenrosePlugin.getImageDescriptor(PenroseImage.NEW));
        setAccelerator(SWT.CTRL | 'N');
        setToolTipText("New Connection...");
        setId(getClass().getName());
	}
	
	public void run() {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        try {
            ProjectConfig projectConfig = new ProjectConfig();
            projectConfig.setName("localhost");
            projectConfig.setHost("localhost");
            projectConfig.setPort(1099);

            ProjectEditorDialog dialog = new ProjectEditorDialog(shell, SWT.NONE);
            dialog.setProjectConfig(projectConfig);
            dialog.open();

            if (dialog.getAction() == ProjectEditorDialog.CANCEL) return;

            ServersView view = ServersView.getInstance();
            view.addProjectConfig(projectConfig);

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    "Failed creating project.\n"+
                            "See penrose-studio.log for details."
            );
        }
	}
	
}