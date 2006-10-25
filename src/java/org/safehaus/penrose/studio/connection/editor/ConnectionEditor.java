package org.safehaus.penrose.studio.connection.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public abstract class ConnectionEditor extends FormEditor {

    public Logger log = Logger.getLogger(getClass());

    private Server server;
    private Partition partition;

    private ConnectionConfig originalConnectionConfig;
    private ConnectionConfig connectionConfig;

    private boolean dirty;

    public void setInput(IEditorInput input) {
        super.setInput(input);

        ConnectionEditorInput ei = (ConnectionEditorInput)input;
        server = ei.getServer();
        partition = ei.getPartition();
        originalConnectionConfig = ei.getConnectionConfig();
        connectionConfig = (ConnectionConfig)originalConnectionConfig.clone();

        setPartName(partition.getName()+"/"+connectionConfig.getName());
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

    public void refresh() {
        for (Iterator i=pages.iterator(); i.hasNext(); ) {
            ConnectionEditorPage page = (ConnectionEditorPage)i.next();
            page.refresh();
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

    public void doSave(IProgressMonitor iProgressMonitor) {
        try {
            store();
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void doSaveAs() {
    }

    public void store() throws Exception {

        if (!getOriginalConnectionConfig().getName().equals(getConnectionConfig().getName())) {
            getPartition().renameConnectionConfig(getOriginalConnectionConfig(), getConnectionConfig().getName());

            for (Iterator i=getPartition().getSourceConfigs().iterator(); i.hasNext(); ) {
                SourceConfig sourceConfig = (SourceConfig)i.next();
                if (!sourceConfig.getConnectionName().equals(getOriginalConnectionConfig().getName())) continue;
                sourceConfig.setConnectionName(getConnectionConfig().getName());
            }
        }

        getPartition().modifyConnectionConfig(getConnectionConfig().getName(), getConnectionConfig());

        setPartName(getPartition().getName()+"/"+getConnectionConfig().getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();

        checkDirty();
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
