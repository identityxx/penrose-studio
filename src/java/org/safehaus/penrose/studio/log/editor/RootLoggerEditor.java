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
package org.safehaus.penrose.studio.log.editor;

import org.eclipse.ui.PartInitException;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.editor.Editor;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.log.tree.LoggersNode;
import org.safehaus.penrose.log.LogManagerClient;
import org.safehaus.penrose.log.log4j.RootLoggerConfig;

public class RootLoggerEditor extends Editor {

    ServerNode serverNode;
    LoggersNode loggersNode;

    RootLoggerConfig rootLoggerConfig;

    public void init() throws PartInitException {

        RootLoggerEditorInput ei = (RootLoggerEditorInput)getEditorInput();
        loggersNode = ei.getLoggersNode();

        try {
            serverNode = loggersNode.getLogsNode().getServerNode();
            PenroseClient client = serverNode.getServer().getClient();
            LogManagerClient logManagerClient = client.getLogManagerClient();
            rootLoggerConfig = logManagerClient.getRootLoggerConfig();

        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }
    }

    protected void addPages() {
        try {
            addPage(new RootLoggerPropertiesEditorPage(this));
            addPage(new RootLoggerAppendersEditorPage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void store() throws Exception {

        PenroseClient client = serverNode.getServer().getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();
        logManagerClient.updateRootLogger(rootLoggerConfig);
        logManagerClient.store();
    }

    public ServerNode getServerNode() {
        return serverNode;
    }

    public void setServerNode(ServerNode serverNode) {
        this.serverNode = serverNode;
    }
}