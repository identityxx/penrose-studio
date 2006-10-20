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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.event.ChangeListener;
import org.safehaus.penrose.studio.event.SelectionListener;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.server.Server;
import org.apache.log4j.Logger;

public class SaveAction extends Action implements ChangeListener, SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public SaveAction() {
        setText("&Save");
        setImageDescriptor(PenrosePlugin.getImageDescriptor(PenroseImage.SAVE));
        setAccelerator(SWT.CTRL | 'S');
        setToolTipText("Save");
        setId(getClass().getName());
    }

    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        try {
            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            Node node = penroseStudio.getSelectedNode();
            if (node == null) return;

            IWorkbenchPage page = window.getActivePage();
            page.saveAllEditors(false);

            ServerNode serverNode = (ServerNode)node;
            Server server = serverNode.getProject();
            server.save();

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    e.getMessage()
            );
        }
    }

    public void updateStatus(Object object) {
        if (object instanceof ServerNode) {
            ServerNode serverNode = (ServerNode)object;
            Server server = serverNode.getProject();
            setEnabled(server.isConnected());

        } else {
            setEnabled(false);
        }
    }

    public void objectChanged(ChangeEvent event) {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Node node = penroseStudio.getSelectedNode();
        updateStatus(node);
    }

    public void objectSelected(SelectionEvent event) {
        Object object = event.getObject();
        updateStatus(object);
    }
}