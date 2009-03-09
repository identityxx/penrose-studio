package org.safehaus.penrose.studio.connection.editor;

import org.eclipse.ui.PartInitException;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.editor.Editor;
import org.safehaus.penrose.studio.server.Server;

/**
 * @author Endi Sukma Dewata
 */
public abstract class ConnectionEditor extends Editor {

    protected Server server;
    protected String partitionName;
    protected String connectionName;

    protected ConnectionConfig connectionConfig;

    public void init() throws PartInitException {
        
        ConnectionEditorInput ei = (ConnectionEditorInput)getEditorInput();
        server = ei.getServer();
        partitionName = ei.getPartitionName();
        connectionName = ei.getConnectionName();

        try {
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

            ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(connectionName);
            connectionConfig = connectionClient.getConnectionConfig();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
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

    public void rename(String name, String newName) throws Exception {

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

        connectionManagerClient.renameConnection(name, newName);
        partitionClient.store();

        connectionConfig.setName(newName);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void store() throws Exception {

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
        connectionManagerClient.updateConnection(connectionName, connectionConfig);
        partitionClient.store();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }
}
