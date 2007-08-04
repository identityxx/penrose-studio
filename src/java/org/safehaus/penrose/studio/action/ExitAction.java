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
package org.safehaus.penrose.studio.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.safehaus.penrose.studio.PenroseStudio;

public class ExitAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public ExitAction() {
		
	}

	public void run(IAction action) {
		boolean confirmed = true;
		
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        if (penroseStudio.isDirty()) {
            confirmed = MessageDialog.openConfirm(
                    window.getShell(),
                    "Confirm Exit",
                    "Are you sure you want to exit?\nYou will lose unsaved works!");
        }

		if (confirmed) {
			System.exit(0);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
        this.window = window;
	}
}