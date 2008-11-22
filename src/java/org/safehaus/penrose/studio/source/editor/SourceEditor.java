package org.safehaus.penrose.studio.source.editor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.Project;

/**
 * @author Endi Sukma Dewata
 */
public abstract class SourceEditor extends FormEditor {

    public Logger log = Logger.getLogger(getClass());

    protected boolean dirty;

    protected Project project;
    protected String partitionName;
    protected String origSourceName;

    protected SourceConfig origSourceConfig;
    protected SourceConfig sourceConfig;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        //setSite(site);
        //setInput(input);

        SourceEditorInput ei = (SourceEditorInput)input;
        project = ei.getProject();
        partitionName = ei.getPartitionName();
        origSourceName = ei.getSourceName();

        try {
            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

            SourceClient sourceClient = sourceManagerClient.getSourceClient(origSourceName);
            origSourceConfig = sourceClient.getSourceConfig();

            sourceConfig = (SourceConfig)origSourceConfig.clone();

        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }

        setPartName(ei.getName());

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

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
/*
        SourceConfigManager sourceConfigManager = partitionConfig.getSourceConfigManager();
        if (!origSourceName.equals(sourceConfig.getName())) {

            sourceConfigManager.renameSourceConfig(origSourceConfig, sourceConfig.getName());

            for (EntryConfig entryConfig : partitionConfig.getDirectoryConfig().getEntryConfigs()) {

                for (SourceMapping sourceMapping : entryConfig.getSourceConfigs()) {
                    if (!sourceMapping.getSourceName().equals(origSourceConfig.getName())) continue;
                    sourceMapping.setSourceName(sourceConfig.getName());
                }
            }
        }

        sourceConfigManager.modifySourceConfig(sourceConfig.getName(), sourceConfig);
        project.save(partitionConfig, sourceConfigManager);
*/
        sourceManagerClient.updateSource(origSourceName, sourceConfig);
        partitionClient.store();

        setPartName(partitionName+"."+sourceConfig.getName());

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

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void checkDirty() {
        try {
            dirty = false;

            if (!origSourceConfig.equals(sourceConfig)) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }
}
