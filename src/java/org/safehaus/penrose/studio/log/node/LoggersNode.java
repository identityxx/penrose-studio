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

import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.action.RefreshAction;
import org.safehaus.penrose.studio.log.node.LogsNode;
import org.safehaus.penrose.studio.log.dialog.LoggerDialog;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.log.log4j.RootConfig;
import org.safehaus.penrose.log.log4j.Log4jConfig;
import org.safehaus.penrose.log.log4j.LoggerConfig;
import org.safehaus.penrose.log.LogManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
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

    public void init() throws Exception {
        update();
    }

    public void update() throws Exception {

        Server server = logsNode.getServerNode().getServer();
        PenroseClient client = server.getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();

        for (String loggerName : logManagerClient.getLoggerConfigNames()) {

            LoggerNode loggerNode = new LoggerNode(
                    view,
                    loggerName,
                    this
            );

            children.add(loggerNode);
        }
    }

    public void refresh() throws Exception {
        children.clear();
        update();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Root Logger") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("New Logger...") {
            public void run() {
                try {
                    createLogger();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new RefreshAction(this));
    }

    public void open() throws Exception {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Log4jConfig loggingConfig = penroseStudio.getLoggingConfig();

        RootConfig rootConfig = loggingConfig.getRootConfig();
        if (rootConfig == null) rootConfig = new RootConfig();

        LoggerDialog dialog = new LoggerDialog(view.getSite().getShell(), SWT.NONE);
        dialog.setText("Edit Logger");
        dialog.setRootConfig(rootConfig);
        dialog.open();

        if (dialog.getAction() == LoggerDialog.CANCEL) return;

        if (rootConfig.getLevel() == null && rootConfig.getAppenderNames().isEmpty()) {
            loggingConfig.setRootConfig(null);

        } else {
            if (loggingConfig.getRootConfig() == null) loggingConfig.setRootConfig(rootConfig);
        }
    }

    public void createLogger() throws Exception {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Log4jConfig loggingConfig = penroseStudio.getLoggingConfig();

        LoggerConfig loggerConfig = new LoggerConfig();

        LoggerDialog dialog = new LoggerDialog(view.getSite().getShell(), SWT.NONE);
        dialog.setText("Add Logger");
        dialog.setLoggerConfig(loggerConfig);
        dialog.open();

        if (dialog.getAction() == LoggerDialog.CANCEL) return;

        loggingConfig.addLoggerConfig(loggerConfig);

        penroseStudio.notifyChangeListeners();
    }

    public LogsNode getLogsNode() {
        return logsNode;
    }

    public void setLogsNode(LogsNode logsNode) {
        this.logsNode = logsNode;
    }
}
