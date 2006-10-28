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
package org.safehaus.penrose.studio.schema;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.schema.editor.SchemaEditor;
import org.safehaus.penrose.studio.schema.editor.SchemaEditorInput;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.schema.SchemaConfig;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.config.PenroseConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class SchemaNode extends Node {

    Logger log = Logger.getLogger(getClass());

    Server server;

    private SchemaConfig schemaConfig;

    public SchemaNode(
            Server server,
            String name,
            Image image,
            Object object,
            Node parent
    ) {
        super(name, image, object, parent);
        this.server = server;
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(actions.getOpenAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

/*
        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
*/
        manager.add(actions.getOpenAction());
    }

    public void open() throws Exception {
        SchemaManager schemaManager = server.getSchemaManager();
        Schema schema = schemaManager.getSchema(schemaConfig.getName());

        SchemaEditorInput ei = new SchemaEditorInput();
        ei.setProject(server);
        ei.setSchemaConfig(schemaConfig);
        ei.setSchema(schema);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, SchemaEditor.class.getName());
    }

    public void delete() throws Exception {
        PenroseConfig penroseConfig = server.getPenroseConfig();
        penroseConfig.removeSchemaConfig(schemaConfig.getName());

        SchemaManager schemaManager = server.getSchemaManager();
        schemaManager.removeSchema(schemaConfig.getName());
    }

    public boolean hasChildren() throws Exception {
        return false;
    }

    public SchemaConfig getSchemaConfig() {
        return schemaConfig;
    }

    public void setSchemaConfig(SchemaConfig schemaConfig) {
        this.schemaConfig = schemaConfig;
    }
}
