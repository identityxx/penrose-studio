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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.swt.graphics.Image;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.partition.PartitionNode;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.plugin.PluginManager;
import org.safehaus.penrose.studio.plugin.Plugin;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.connection.action.NewSourceAction;
import org.safehaus.penrose.studio.connection.editor.*;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionConfigs;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class ConnectionNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ServersView view;
    private ProjectNode projectNode;
    private PartitionsNode partitionsNode;
    private PartitionNode partitionNode;
    private ConnectionsNode connectionsNode;

    private PartitionConfig partitionConfig;
    private ConnectionConfig connectionConfig;

    public ConnectionNode(String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        connectionsNode = (ConnectionsNode)parent;
        partitionNode = connectionsNode.getPartitionNode();
        partitionsNode = partitionNode.getPartitionsNode();
        projectNode = partitionsNode.getProjectNode();
        this.view = projectNode.getServersView();
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

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new NewSourceAction(this));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Copy") {
            public void run() {
                try {
                    copy();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Delete", PenroseStudioPlugin.getImageDescriptor(PenroseImage.SIZE_16x16, PenroseImage.DELETE)) {
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

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PluginManager pluginManager = penroseStudio.getPluginManager();
        Plugin plugin = pluginManager.getPlugin(connectionConfig.getAdapterName());

        ConnectionEditorInput ei = plugin.createConnectionEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setPartitionConfig(partitionConfig);
        ei.setConnectionConfig(connectionConfig);

        String connectionEditorClass = plugin.getConnectionEditorClass();

        log.debug("Opening "+connectionEditorClass);
        page.openEditor(ei, connectionEditorClass);
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Confirmation",
                "Remove Connection \""+connectionConfig.getName()+"\"?");

        if (!confirm) return;

        ConnectionConfigs connectionConfigs = partitionConfig.getConnectionConfigs();
        connectionConfigs.removeConnectionConfig(connectionConfig.getName());

        Project project = projectNode.getProject();
        project.save(partitionConfig, connectionConfigs);
        
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void copy() throws Exception {
        view.setClipboard(connectionConfig);
    }

    public void paste() throws Exception {

        Object newObject = view.getClipboard();

        if (!(newObject instanceof ConnectionConfig)) return;

        ConnectionConfigs connectionConfigs = partitionConfig.getConnectionConfigs();

        ConnectionConfig newConnectionConfig = (ConnectionConfig)((ConnectionConfig)newObject).clone();

        int counter = 1;
        String name = newConnectionConfig.getName();
        while (connectionConfigs.getConnectionConfig(name) != null) {
            counter++;
            name = newConnectionConfig.getName()+" ("+counter+")";
        }

        newConnectionConfig.setName(name);
        connectionConfigs.addConnectionConfig(newConnectionConfig);

        view.setClipboard(null);

        Project project = projectNode.getProject();
        project.save(partitionConfig, connectionConfigs);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        return false;
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        return children;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public ServersView getView() {
        return view;
    }

    public void setView(ServersView view) {
        this.view = view;
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }

    public PartitionsNode getPartitionsNode() {
        return partitionsNode;
    }

    public void setPartitionsNode(PartitionsNode partitionsNode) {
        this.partitionsNode = partitionsNode;
    }

    public PartitionNode getPartitionNode() {
        return partitionNode;
    }

    public void setPartitionNode(PartitionNode partitionNode) {
        this.partitionNode = partitionNode;
    }

    public ConnectionsNode getConnectionsNode() {
        return connectionsNode;
    }

    public void setConnectionsNode(ConnectionsNode connectionsNode) {
        this.connectionsNode = connectionsNode;
    }
}
