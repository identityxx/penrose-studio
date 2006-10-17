package org.safehaus.penrose.studio.connection;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorInput;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.ConnectionConfig;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public abstract class ConnectionEditor extends FormEditor {

    public Logger log = Logger.getLogger(getClass());

    private Partition partition;

    private ConnectionConfig originalConnectionConfig;
    private ConnectionConfig connectionConfig;

    private boolean dirty;

    public void setInput(IEditorInput input) {
        super.setInput(input);

        ConnectionEditorInput cei = (ConnectionEditorInput)input;
        partition = cei.getPartition();
        originalConnectionConfig = cei.getConnectionConfig();
        connectionConfig = (ConnectionConfig)originalConnectionConfig.clone();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void checkDirty() {
        try {
            dirty = false;

            if (!originalConnectionConfig.equals(connectionConfig)) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.debug(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
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

    public ConnectionConfig getOriginalConnectionConfig() {
        return originalConnectionConfig;
    }

    public void setOriginalConnectionConfig(ConnectionConfig originalConnectionConfig) {
        this.originalConnectionConfig = originalConnectionConfig;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
