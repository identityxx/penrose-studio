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

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.connection.action.NewSourceAction;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorInput;
import org.safehaus.penrose.studio.partition.PartitionNode;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.plugin.Plugin;
import org.safehaus.penrose.studio.plugin.PluginManager;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

import java.util.Collection;

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

    private String partitionName;
    private String adapterName;
    private String connectionName;

    public ConnectionNode(String name, Image image, Object object, Object parent) {
        super(name, image, object, parent);
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
                    throw new RuntimeException(e.getMessage(), e);
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
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Delete", PenroseStudioPlugin.getImageDescriptor(PenroseImage.SIZE_16x16, PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PluginManager pluginManager = penroseStudio.getPluginManager();
        Plugin plugin = pluginManager.getPlugin(adapterName);

        ConnectionEditorInput ei = plugin.createConnectionEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setPartitionName(partitionName);
        ei.setConnectionName(connectionName);

        String connectionEditorClass = plugin.getConnectionEditorClass();

        log.debug("Opening "+connectionEditorClass);
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, connectionEditorClass);
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Confirmation",
                "Remove selected connections?");

        if (!confirm) return;

        Project project = projectNode.getProject();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        //ConnectionConfigManager connectionConfigManager = partitionConfig.getConnectionConfigManager();

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof ConnectionNode)) continue;

            ConnectionNode connectionNode = (ConnectionNode)node;
            partitionClient.removeConnection(connectionNode.getConnectionName());
        }

        partitionClient.store();
        //project.save(partitionConfig, connectionConfigManager);
        
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void copy() throws Exception {

        Project project = projectNode.getProject();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        ConnectionClient connectionClient = partitionClient.getConnectionClient(connectionName);
        view.setClipboard(connectionClient.getConnectionConfig());
    }

    public void paste() throws Exception {

        Object newObject = view.getClipboard();

        if (!(newObject instanceof ConnectionConfig)) return;

        Project project = projectNode.getProject();

        ConnectionConfig newConnectionConfig = (ConnectionConfig)((ConnectionConfig)newObject).clone();
        view.setClipboard(null);
/*
        ConnectionConfigManager connectionConfigManager = partitionConfig.getConnectionConfigManager();
        int counter = 1;
        String name = newConnectionConfig.getName();
        while (connectionConfigManager.getConnectionConfig(name) != null) {
            counter++;
            name = newConnectionConfig.getName()+" ("+counter+")";
        }
        newConnectionConfig.setName(name);

        connectionConfigManager.addConnectionConfig(newConnectionConfig);
        project.save(partitionConfig, connectionConfigManager);
*/
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        Collection<String> connectionNames = partitionClient.getConnectionNames();
        int counter = 1;
        String name = newConnectionConfig.getName();
        while (connectionNames.contains(name)) {
            counter++;
            name = newConnectionConfig.getName()+" ("+counter+")";
        }
        newConnectionConfig.setName(name);

        partitionClient.createConnection(newConnectionConfig);
        partitionClient.store();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
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

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }
}
