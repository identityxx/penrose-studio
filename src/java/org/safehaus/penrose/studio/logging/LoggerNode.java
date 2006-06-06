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
package org.safehaus.penrose.studio.logging;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.log4j.LoggerConfig;
import org.safehaus.penrose.log4j.Log4jConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class LoggerNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;
    LoggerConfig loggerConfig;

    public LoggerNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
        this.loggerConfig = (LoggerConfig)object;
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

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        LoggerDialog dialog = new LoggerDialog(shell, SWT.NONE);
        dialog.setText("Edit Logger");
        dialog.setLoggerConfig(loggerConfig);
        dialog.open();
    }

    public void remove() throws Exception {
        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        Log4jConfig loggingConfig = penroseApplication.getLoggingConfig();
        loggingConfig.removeLoggerConfig(loggerConfig.getName());
        penroseApplication.notifyChangeListeners();
    }
}
