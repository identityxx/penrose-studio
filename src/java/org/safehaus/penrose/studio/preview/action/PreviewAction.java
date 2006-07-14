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
package org.safehaus.penrose.studio.preview.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.preview.PreviewEditorInput;
import org.safehaus.penrose.studio.preview.PreviewEditor;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class PreviewAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public PreviewAction() {

        setText("&Preview");
        setImageDescriptor(PenrosePlugin.getImageDescriptor(PenroseImage.PREVIEW));
        setToolTipText("Directory Preview");
        setId(getClass().getName());
    }

	public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        try {
            IWorkbenchPage activePage = window.getActivePage();

            //activePage.showView(ConsoleView.class.getName());
            activePage.openEditor(new PreviewEditorInput(), PreviewEditor.class.getName());

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            String message = e.toString();
            if (message.length() > 500) {
                message = message.substring(0, 500) + "...";
            }
            MessageDialog.openError(window.getShell(), "Preview Failed", message);
        }
	}
}