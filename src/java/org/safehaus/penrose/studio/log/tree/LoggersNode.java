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
package org.safehaus.penrose.studio.log.tree;

import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.action.RefreshAction;
import org.safehaus.penrose.studio.log.wizard.LoggerWizard;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.log.log4j.LoggerConfig;
import org.safehaus.penrose.log.LogManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class LoggersNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;
    LogsNode logsNode;

    public LoggersNode(ServersView view, String name, LogsNode logsNode) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, logsNode);
        this.view = view;
        this.logsNode = logsNode;
    }

    public void update() throws Exception {

        Server server = logsNode.getServerNode().getServer();
        PenroseClient client = server.getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();

        RootLoggerNode rootLoggerNode = new RootLoggerNode(view, this);
        addChild(rootLoggerNode);

        for (String loggerName : logManagerClient.getLoggerNames()) {

            LoggerNode loggerNode = new LoggerNode(
                    view,
                    loggerName,
                    this
            );

            addChild(loggerNode);
        }
    }

    public void expand() throws Exception {
        if (children == null) update();
    }

    public void refresh() throws Exception {
        removeChildren();
        update();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("New Logger...") {
            public void run() {
                try {
                    createLogger();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new RefreshAction(this));
    }

    public void createLogger() throws Exception {

        PenroseClient client = logsNode.getServerNode().getServer().getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();

        LoggerConfig loggerConfig = new LoggerConfig();

        LoggerWizard wizard = new LoggerWizard();
        wizard.setLogManagerClient(logManagerClient);
        wizard.setLoggerConfig(loggerConfig);

        WizardDialog dialog = new WizardDialog(view.getSite().getShell(), wizard);
        dialog.setPageSize(600, 300);
        int rc = dialog.open();

        if (rc == Window.CANCEL) return;

        logManagerClient.addLogger(loggerConfig);
        logManagerClient.store();

        ServersView serversView = ServersView.getInstance();
        serversView.refresh(this);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public LogsNode getLogsNode() {
        return logsNode;
    }

    public void setLogsNode(LogsNode logsNode) {
        this.logsNode = logsNode;
    }
}
