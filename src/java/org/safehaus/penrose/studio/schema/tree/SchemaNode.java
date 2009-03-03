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
package org.safehaus.penrose.studio.schema.tree;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.schema.editor.SchemaEditor;
import org.safehaus.penrose.studio.schema.editor.SchemaEditorInput;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class SchemaNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;
    ServerNode serverNode;
    SchemasNode schemasNode;

    String schemaName;

    public SchemaNode(String schemaName, SchemasNode parent) {
        super(schemaName, PenroseStudio.getImage(PenroseImage.SCHEMA), null, parent);
        
        this.schemaName = schemaName;
        schemasNode = parent;
        serverNode = schemasNode.getServerNode();
        view = serverNode.getServersView();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
/*
        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
*/
        manager.add(new Action("Delete", PenroseStudio.getImageDescriptor(PenroseImage.DELETE_SMALL)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        Server server = serverNode.getServer();

        SchemaEditorInput ei = new SchemaEditorInput();
        ei.setServer(server);
        ei.setSchemaName(schemaName);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, SchemaEditor.class.getName());
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Confirmation",
                "Remove Schema \""+ schemaName+"\"?");

        if (!confirm) return;

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        
        SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
        schemaManagerClient.removeSchema(schemaName);

        ServersView serversView = ServersView.getInstance();
        serversView.refresh(parent);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public ServerNode getServerNode() {
        return serverNode;
    }

    public void setServerNode(ServerNode serverNode) {
        this.serverNode = serverNode;
    }

    public SchemasNode getSchemasNode() {
        return schemasNode;
    }

    public void setSchemasNode(SchemasNode schemasNode) {
        this.schemasNode = schemasNode;
    }

    public boolean hasChildren() {
        return false;
    }
}
