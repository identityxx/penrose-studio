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

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.node.ServerNode;
import org.safehaus.penrose.studio.log.node.LoggersNode;
import org.safehaus.penrose.log.LogManagerClient;
import org.safehaus.penrose.log.log4j.LoggerConfig;

public class LoggerEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());

    ServerNode serverNode;
    LoggersNode loggersNode;
    String loggerName;

    LoggerConfig loggerConfig;

    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        LoggerEditorInput ei = (LoggerEditorInput)input;

        loggersNode = ei.getLoggersNode();
        loggerName = ei.getLoggerName();

        try {

            serverNode = loggersNode.getLogsNode().getServerNode();
            PenroseClient client = serverNode.getServer().getClient();
            LogManagerClient logManagerClient = client.getLogManagerClient();
            loggerConfig = logManagerClient.getLoggerConfig(loggerName);

        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }

        setPartName(loggerName);
    }

    protected void addPages() {
        try {
            addPage(new LoggerPropertiesEditorPage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
    }

    public void doSaveAs() {
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void store() throws Exception {

        serverNode = loggersNode.getLogsNode().getServerNode();
        PenroseClient client = serverNode.getServer().getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();
        logManagerClient.updateLoggerConfig(loggerName, loggerConfig);

        setPartName(loggerName);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }
}