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
package org.safehaus.penrose.studio.logging;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.logging.editor.LoggerDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.log4j.LoggerConfig;
import org.safehaus.penrose.log4j.Log4jConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class LoggerNode extends Node {

    Logger log = Logger.getLogger(getClass());

    Server server;
    LoggerConfig loggerConfig;

    public LoggerNode(
            Server server,
            String name,
            Image image,
            Object object,
            Node parent
    ) {
        super(name, image, object, parent);
        this.server = server;
        this.loggerConfig = (LoggerConfig)object;
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(actions.getOpenAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getCopyAction());
        manager.add(actions.getPasteAction());

        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        Log4jConfig log4jConfig = server.getLog4jConfig();

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        LoggerDialog dialog = new LoggerDialog(shell, SWT.NONE);
        dialog.setText("Edit Logger");
        dialog.setLog4jConfig(log4jConfig);
        dialog.setLoggerConfig(loggerConfig);
        dialog.open();
    }

    public Object copy() throws Exception {
        return loggerConfig;
    }

    public boolean canPaste(Object object) throws Exception {
        return getParent().canPaste(object);
    }

    public void paste(Object object) throws Exception {
        getParent().paste(object);
    }

    public void remove() throws Exception {
        Log4jConfig log4jConfig = server.getLog4jConfig();
        log4jConfig.removeLoggerConfig(loggerConfig.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();
    }
}
