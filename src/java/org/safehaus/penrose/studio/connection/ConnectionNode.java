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
package org.safehaus.penrose.studio.connection;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.swt.graphics.Image;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.connection.action.NewSourceAction;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorInput;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditor;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class ConnectionNode extends Node {

    Logger log = Logger.getLogger(getClass());


    private Server server;
    private Partition partition;
    private ConnectionConfig connectionConfig;

    public ConnectionNode(String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(actions.getOpenAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new NewSourceAction(this));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getCopyAction());
        manager.add(actions.getPasteAction());
        manager.add(actions.getDeleteAction());
    }

    public void open() throws Exception {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        ConnectionEditorInput ei = new ConnectionEditorInput();
        ei.setServer(server);
        ei.setPartition(partition);
        ei.setConnectionConfig(connectionConfig);

        page.openEditor(ei, ConnectionEditor.class.getName());
    }

    public Object copy() throws Exception {
        return connectionConfig;
    }

    public boolean canPaste(Object object) throws Exception {
        return object instanceof ConnectionConfig;
    }

    public void paste(Object object) throws Exception {
        ConnectionConfig newConnectionConfig = (ConnectionConfig)object;

        int counter = 1;
        String name = newConnectionConfig.getName();
        while (partition.getConnectionConfig(name) != null) {
            counter++;
            name = newConnectionConfig.getName()+" ("+counter+")";
        }

        newConnectionConfig.setName(name);
        partition.addConnectionConfig(newConnectionConfig);
    }

    public void delete() throws Exception {
        partition.removeConnectionConfig(connectionConfig.getName());
    }

    public boolean hasChildren() throws Exception {
        return false;
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        return children;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
