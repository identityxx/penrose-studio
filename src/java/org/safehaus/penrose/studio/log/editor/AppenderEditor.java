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
package org.safehaus.penrose.studio.log.editor;

import org.eclipse.ui.PartInitException;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.config.editor.ParametersPage;
import org.safehaus.penrose.studio.editor.Editor;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.log.tree.AppendersNode;
import org.safehaus.penrose.log.LogManagerClient;
import org.safehaus.penrose.log.log4j.AppenderConfig;

public class AppenderEditor extends Editor {

    ServerNode serverNode;
    AppendersNode appendersNode;
    String appenderName;

    AppenderConfig appenderConfig;

    public void init() throws PartInitException {

        AppenderEditorInput ei = (AppenderEditorInput)getEditorInput();
        appendersNode = ei.getAppendersNode();
        appenderName = ei.getAppenderName();

        try {
            serverNode = appendersNode.getLogsNode().getServerNode();
            PenroseClient client = serverNode.getServer().getClient();
            LogManagerClient logManagerClient = client.getLogManagerClient();
            appenderConfig = logManagerClient.getAppenderConfig(appenderName);

        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }
    }

    protected void addPages() {
        try {
            addPage(new AppenderPropertiesEditorPage(this));

            LayoutEditorPage layoutPage = new LayoutEditorPage(this);
            layoutPage.setLayoutConfig(appenderConfig.getLayoutConfig());
            addPage(layoutPage);

            ParametersPage parametersPage = new ParametersPage(this, "Appender Editor");
            parametersPage.setParameters(appenderConfig.getParameters());
            addPage(parametersPage);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void store() throws Exception {

        PenroseClient client = serverNode.getServer().getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();
        logManagerClient.updateAppender(appenderName, appenderConfig);
        logManagerClient.store();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }
}