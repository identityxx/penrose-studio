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
package org.safehaus.penrose.studio.log.node;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.log.editor.LoggerEditorInput;
import org.safehaus.penrose.studio.log.editor.LoggerEditor;
import org.safehaus.penrose.log.LogManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class LoggerNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;
    LoggersNode loggersNode;
    String loggerName;

    public LoggerNode(ServersView view, String loggerName, LoggersNode loggersNode) {
        super(loggerName, PenroseStudio.getImage(PenroseImage.LOGGER), null, loggersNode);

        this.view = view;
        this.loggersNode = loggersNode;
        this.loggerName = loggerName;
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

        LoggerEditorInput ei = new LoggerEditorInput();
        ei.setLoggersNode(loggersNode);
        ei.setLoggerName(loggerName);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, LoggerEditor.class.getName());
/*
        Server server = loggersNode.getLogsNode().getServerNode().getServer();
        PenroseClient client = server.getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();
        LoggerConfig loggerConfig = logManagerClient.getLoggerConfig(loggerName);

        LoggerDialog dialog = new LoggerDialog(view.getSite().getShell(), SWT.NONE);
        dialog.setText("Edit Logger");
        dialog.setLoggerConfig(loggerConfig);
        dialog.open();
*/
    }

    public void remove() throws Exception {

        Shell shell = view.getSite().getShell();

        boolean confirm = MessageDialog.openQuestion(
                shell,
                "Confirmation",
                "Remove Logger \""+loggerName+"\"?");

        if (!confirm) return;

        Server server = loggersNode.getLogsNode().getServerNode().getServer();
        PenroseClient client = server.getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof LoggerNode)) continue;

            LoggerNode loggerNode = (LoggerNode)node;
            logManagerClient.removeAppenderConfig(loggerNode.getLoggerName());
        }

        parent.refresh();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }
}
