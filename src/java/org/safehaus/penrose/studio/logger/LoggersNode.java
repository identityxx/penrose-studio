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
package org.safehaus.penrose.studio.logger;

import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.schema.action.ImportSchemaAction;
import org.safehaus.penrose.studio.schema.action.NewSchemaAction;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.management.PenroseClient;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.PlatformUI;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class LoggersNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    public LoggersNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {
        manager.add(new NewSchemaAction());
        manager.add(new ImportSchemaAction());
    }

    public void open() throws Exception {
        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseClient client = penroseApplication.getClient();
        String level = client.getLoggerLevel(null);

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        LoggerDialog dialog = new LoggerDialog(shell, SWT.NONE);
        dialog.setText("Logger");
        dialog.setLoggerName("Root Logger");
        dialog.setLoggerLevel(level);
        dialog.open();

        if (dialog.getAction() == LoggerDialog.CANCEL) return;

        String newLevel = dialog.getLoggerLevel();
        if (level == null && newLevel == null || level.equals(newLevel)) return;

        client.setLoggerLevel(null, newLevel);
    }

    public boolean hasChildren() throws Exception {
        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        LoggerManager loggerManager = penroseApplication.getLoggerManager();
        Collection loggers = loggerManager.getLoggers();
        if (loggers == null) return false;
        return !loggers.isEmpty();
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        LoggerManager loggerManager = penroseApplication.getLoggerManager();

        for (Iterator i=loggerManager.getLoggers().iterator(); i.hasNext(); ) {
            String loggerName = (String)i.next();

            LoggerNode loggerNode = new LoggerNode(
                    view,
                    loggerName,
                    ObjectsView.LOGGER,
                    PenrosePlugin.getImage(PenroseImage.SCHEMA),
                    loggerName,
                    this
            );

            children.add(loggerNode);
        }

        return children;
    }
}
