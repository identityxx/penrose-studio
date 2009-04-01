/**
 * Copyright 2009 Red Hat, Inc.
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
package org.safehaus.penrose.studio.log.tree;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.log.editor.RootLoggerEditorInput;
import org.safehaus.penrose.studio.log.editor.RootLoggerEditor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Endi S. Dewata
 */
public class RootLoggerNode extends Node {

    ServersView view;
    LoggersNode loggersNode;

    public RootLoggerNode(ServersView view, LoggersNode loggersNode) {
        super("Root Logger", PenroseStudio.getImage(PenroseImage.LOGGER), null, loggersNode);

        this.view = view;
        this.loggersNode = loggersNode;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });
    }

    public void open() throws Exception {

        RootLoggerEditorInput ei = new RootLoggerEditorInput();
        ei.setLoggersNode(loggersNode);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, RootLoggerEditor.class.getName());
/*
        Server server = loggersNode.getLogsNode().getServerNode().getServer();
        PenroseClient client = server.getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();
        LoggerConfig loggerConfig = logManagerClient.getRootLoggerConfig();

        LoggerDialog dialog = new LoggerDialog(view.getSite().getShell(), SWT.NONE);
        dialog.setText("Edit Logger");
        dialog.updateRootLogger(loggerConfig);
        dialog.open();
*/
    }

    public boolean hasChildren() {
        return false;
    }
}