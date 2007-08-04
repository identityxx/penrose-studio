package org.safehaus.penrose.studio.connection.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.apache.log4j.Logger;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseWorkbenchAdvisor;
import org.safehaus.penrose.studio.PenroseWorkbenchWindowAdvisor;
import org.safehaus.penrose.studio.PenroseActionBarAdvisor;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class NISConnectionEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());

    boolean dirty;

    private PartitionConfig partitionConfig;
    ConnectionConfig origConnectionConfig;
    private ConnectionConfig connectionConfig;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        ConnectionEditorInput cei = (ConnectionEditorInput)input;

        partitionConfig = cei.getPartitionConfig();
        origConnectionConfig = cei.getConnectionConfig();

        try {
            connectionConfig = (ConnectionConfig)origConnectionConfig.clone();
        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }

        setSite(site);
        setInput(input);
        setPartName(partitionConfig.getName()+"/"+connectionConfig.getName());
    }

    public void addPages() {
        try {
            addPage(new NISConnectionPropertiesPage(this));

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            PenroseWorkbenchAdvisor workbenchAdvisor = penroseStudio.getWorkbenchAdvisor();
            PenroseWorkbenchWindowAdvisor workbenchWindowAdvisor = workbenchAdvisor.getWorkbenchWindowAdvisor();
            PenroseActionBarAdvisor actionBarAdvisor = workbenchWindowAdvisor.getActionBarAdvisor();

            //if (actionBarAdvisor.getShowCommercialFeaturesAction().isChecked()) {

                //addPage(new JNDIConnectionBrowserPage(this));
                //addPage(new JNDIConnectionSchemaPage(this));
            //}

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
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

        if (!origConnectionConfig.getName().equals(connectionConfig.getName())) {
            partitionConfig.getConnectionConfigs().renameConnectionConfig(origConnectionConfig, connectionConfig.getName());

            for (Iterator i=partitionConfig.getSourceConfigs().getSourceConfigs().iterator(); i.hasNext(); ) {
                SourceConfig sourceConfig = (SourceConfig)i.next();
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
            log.debug(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
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
}
