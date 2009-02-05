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
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.nis.connection.action.NewNISConnectionAction;
import org.safehaus.penrose.studio.ldap.connection.action.NewLDAPConnectionAction;
import org.safehaus.penrose.studio.jdbc.connection.action.NewJDBCConnectionAction;
import org.safehaus.penrose.studio.partition.node.PartitionNode;
import org.safehaus.penrose.studio.partition.node.PartitionsNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.node.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class ConnectionsNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ServersView view;
    private ServerNode serverNode;
    private PartitionsNode partitionsNode;
    private PartitionNode partitionNode;

    private String partitionName;

    public ConnectionsNode(String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
        partitionNode = (PartitionNode)parent;
        partitionsNode = partitionNode.getPartitionsNode();
        serverNode = partitionsNode.getProjectNode();
        view = serverNode.getServersView();
    }

    public void init() throws Exception {
        update();
    }

    public void update() throws Exception {

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

        for (String connectionName : connectionManagerClient.getConnectionNames()) {

            ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(connectionName);

            ConnectionNode connectionNode = new ConnectionNode(
                    connectionName,
                    PenroseStudio.getImage(PenroseImage.CONNECTION),
                    connectionName,
                    this
            );

            connectionNode.setPartitionName(partitionName);
            connectionNode.setAdapterName(connectionClient.getAdapterName());
            connectionNode.setConnectionName(connectionName);

            children.add(connectionNode);
        }
    }

    public void refresh() throws Exception {
        children.clear();
        update();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new NewJDBCConnectionAction(this));
        manager.add(new NewLDAPConnectionAction(this));
        manager.add(new NewNISConnectionAction(this));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

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

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Refresh") {
            public void run() {
                try {
                    refresh();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
    }

    public void paste() throws Exception {

        Object newObject = view.getClipboard();

        if (!(newObject instanceof ConnectionConfig)) return;

        Server server = serverNode.getServer();

        ConnectionConfig newConnectionConfig = (ConnectionConfig)((ConnectionConfig)newObject).clone();
        view.setClipboard(null);

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

        Collection<String> connectionNames = connectionManagerClient.getConnectionNames();

        int counter = 1;
        String name = newConnectionConfig.getName();
        while (connectionNames.contains(name)) {
            counter++;
            name = newConnectionConfig.getName()+" ("+counter+")";
        }
        newConnectionConfig.setName(name);

        connectionManagerClient.createConnection(newConnectionConfig);
        partitionClient.store();

        refresh();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public ServersView getView() {
        return view;
    }

    public void setView(ServersView view) {
        this.view = view;
    }

    public ServerNode getServerNode() {
        return serverNode;
    }

    public void setServerNode(ServerNode serverNode) {
        this.serverNode = serverNode;
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
}
