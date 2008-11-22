package org.safehaus.penrose.studio.connection.editor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.Project;

/**
 * @author Endi Sukma Dewata
 */
public abstract class ConnectionEditor extends FormEditor {

    public Logger log = Logger.getLogger(getClass());

    protected boolean dirty;
    protected Project project;
    protected String partitionName;
    protected String origConnectionName;

    protected ConnectionConfig origConnectionConfig;
    protected ConnectionConfig connectionConfig;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        ConnectionEditorInput ei = (ConnectionEditorInput)input;
        project = ei.getProject();
        partitionName = ei.getPartitionName();
        origConnectionName = ei.getConnectionName();

        try {
            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

            ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(origConnectionName);
            origConnectionConfig = connectionClient.getConnectionConfig();

            connectionConfig = (ConnectionConfig)origConnectionConfig.clone();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }

        setPartName(ei.getName());
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
        try {
            store();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void doSaveAs() {
    }

    public void store() throws Exception {

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();
/*
        ConnectionConfigManager connectionConfigManager = partitionConfig.getConnectionConfigManager();
        if (!origConnectionConfig.getName().equals(connectionConfig.getName())) {

            connectionConfigManager.renameConnectionConfig(origConnectionConfig, connectionConfig.getName());

            for (SourceConfig sourceConfig : partitionConfig.getSourceConfigManager().getSourceConfigManager()) {
                if (!sourceConfig.getConnectionName().equals(origConnectionConfig.getName())) continue;
                sourceConfig.setConnectionName(connectionConfig.getName());
            }
        }

        connectionConfigManager.modifyConnectionConfig(connectionConfig.getName(), connectionConfig);
        project.save(partitionConfig, connectionConfigManager);
*/
        connectionManagerClient.updateConnection(origConnectionName, connectionConfig);
        partitionClient.store();

        setPartName(partitionName+"."+connectionConfig.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();

        checkDirty();
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void checkDirty() {
        try {
            dirty = false;

            if (!origConnectionConfig.equals(connectionConfig)) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }
}
