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

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.management.PenroseClient;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class LoggerNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    public LoggerNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

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

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseClient client = penroseApplication.getClient();
        String loggerName = (String)getObject();
        String level = client.getLoggerLevel(loggerName);

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        LoggerDialog dialog = new LoggerDialog(shell, SWT.NONE);
        dialog.setText("Logger");
        dialog.setLoggerName(loggerName);
        dialog.setLoggerLevel(level);
        dialog.open();

        if (dialog.getAction() == LoggerDialog.CANCEL) return;

        String newLevel = dialog.getLoggerLevel();
        if (level == null && newLevel == null || level.equals(newLevel)) return;

        client.setLoggerLevel(loggerName, newLevel);
    }

    public void remove() throws Exception {
    }

    public boolean hasChildren() throws Exception {
        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        LoggerManager loggerManager = penroseApplication.getLoggerManager();
        String loggerName = (String)getObject();
        return !loggerManager.getLoggers(loggerName).isEmpty();
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        LoggerManager loggerManager = penroseApplication.getLoggerManager();
        String loggerName = (String)getObject();

        for (Iterator i=loggerManager.getLoggers(loggerName).iterator(); i.hasNext(); ) {
            String childName = (String)i.next();

            int p = childName.lastIndexOf(".");
            String rname = childName.substring(p+1);

            LoggerNode loggerNode = new LoggerNode(
                    view,
                    rname,
                    ObjectsView.LOGGER,
                    PenrosePlugin.getImage(PenroseImage.SCHEMA),
                    childName,
                    this
            );

            children.add(loggerNode);
        }

        return children;
    }
}
