package org.safehaus.penrose.studio.source.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceConfigs;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.mapping.SourceMapping;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.apache.log4j.Logger;

/**
 * @author Endi Sukma Dewata
 */
public abstract class SourceEditor extends FormEditor {

    public Logger log = Logger.getLogger(getClass());

    protected boolean dirty;

    protected ProjectNode projectNode;
    protected PartitionConfig partitionConfig;
    protected SourceConfig sourceConfig;
    protected SourceConfig origSourceConfig;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
    }

    public void setInput(IEditorInput input) {
        super.setInput(input);

        SourceEditorInput ei = (SourceEditorInput)input;

        projectNode = ei.getProjectNode();
        setPartitionConfig(ei.getPartitionConfig());
        origSourceConfig = ei.getSourceConfig();

        try {
            setSourceConfig((SourceConfig)origSourceConfig.clone());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        setPartName(getPartitionConfig().getName()+"/"+ getSourceConfig().getName());
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

        SourceConfigs sources = getPartitionConfig().getSourceConfigs();
        if (!origSourceConfig.getName().equals(getSourceConfig().getName())) {

            sources.renameSourceConfig(origSourceConfig, getSourceConfig().getName());

            for (EntryMapping entryMapping : getPartitionConfig().getDirectoryConfigs().getEntryMappings()) {

                for (SourceMapping sourceMapping : entryMapping.getSourceMappings()) {
                    if (!sourceMapping.getSourceName().equals(origSourceConfig.getName())) continue;
                    sourceMapping.setSourceName(getSourceConfig().getName());
                }
            }
        }

        sources.modifySourceConfig(getSourceConfig().getName(), getSourceConfig());

        setPartName(this.getPartitionConfig().getName()+"/"+ getSourceConfig().getName());

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

            if (!origSourceConfig.equals(getSourceConfig())) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }
}
