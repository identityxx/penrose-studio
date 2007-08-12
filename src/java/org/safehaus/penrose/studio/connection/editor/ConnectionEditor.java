package org.safehaus.penrose.studio.connection.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.source.SourceConfig;
import org.apache.log4j.Logger;

/**
 * @author Endi Sukma Dewata
 */
public abstract class ConnectionEditor extends FormEditor {

    public Logger log = Logger.getLogger(getClass());

    protected boolean dirty;
    protected ProjectNode projectNode;
    protected PartitionConfig partitionConfig;
    protected ConnectionConfig origConnectionConfig;
    protected ConnectionConfig connectionConfig;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
    }

    public void setInput(IEditorInput input) {
        super.setInput(input);

        ConnectionEditorInput cei = (ConnectionEditorInput)input;

        projectNode = cei.getProjectNode();
        partitionConfig = cei.getPartitionConfig();
        origConnectionConfig = cei.getConnectionConfig();

        try {
            connectionConfig = (ConnectionConfig)origConnectionConfig.clone();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        setPartName(partitionConfig.getName()+"/"+connectionConfig.getName());
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

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
        try {
            store();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void doSaveAs() {
    }

    public void store() throws Exception {

        if (!origConnectionConfig.getName().equals(connectionConfig.getName())) {
            partitionConfig.getConnectionConfigs().renameConnectionConfig(origConnectionConfig, connectionConfig.getName());

            for (SourceConfig sourceConfig : partitionConfig.getSourceConfigs().getSourceConfigs()) {
                if (!sourceConfig.getConnectionName().equals(origConnectionConfig.getName())) continue;
                sourceConfig.setConnectionName(connectionConfig.getName());
            }
        }

        partitionConfig.getConnectionConfigs().modifyConnectionConfig(connectionConfig.getName(), connectionConfig);

        setPartName(this.partitionConfig.getName()+"/"+connectionConfig.getName());

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

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }
}
