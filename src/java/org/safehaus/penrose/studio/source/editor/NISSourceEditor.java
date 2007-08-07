package org.safehaus.penrose.studio.source.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.apache.log4j.Logger;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceConfigs;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.mapping.SourceMapping;
import org.safehaus.penrose.studio.PenroseStudio;

import java.util.Iterator;

public class NISSourceEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());

    PartitionConfig partitionConfig;
	SourceConfig sourceConfig;
    SourceConfig origSourceConfig;

    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {

        try {
            SourceEditorInput ei = (SourceEditorInput)input;
            partitionConfig = ei.getPartitionConfig();
            origSourceConfig = ei.getSourceConfig();
            sourceConfig = (SourceConfig)origSourceConfig.clone();
    
            setSite(site);
            setInput(input);
            setPartName(partitionConfig.getName()+"/"+sourceConfig.getName());

        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }
    }

    public void addPages() {
        try {
            addPage(new NISSourcePropertyPage(this));
            addPage(new NISSourceBrowsePage(this));
            //addPage(new JNDISourceCachePage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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

        SourceConfigs sources = partitionConfig.getSourceConfigs();
        if (!origSourceConfig.getName().equals(sourceConfig.getName())) {
            sources.renameSourceConfig(origSourceConfig, sourceConfig.getName());

            for (Iterator i=partitionConfig.getDirectoryConfigs().getEntryMappings().iterator(); i.hasNext(); ) {
                EntryMapping entryMapping = (EntryMapping)i.next();
                for (Iterator j=entryMapping.getSourceMappings().iterator(); j.hasNext(); ) {
                    SourceMapping sourceMapping = (SourceMapping)j.next();
                    if (!sourceMapping.getSourceName().equals(origSourceConfig.getName())) continue;
                    sourceMapping.setSourceName(sourceConfig.getName());
                }
            }
        }

        sources.modifySourceConfig(sourceConfig.getName(), sourceConfig);

        setPartName(this.partitionConfig.getName()+"/"+sourceConfig.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();

        setDirty(false);
	}

    public boolean isDirty() {
        return dirty;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setDirty(boolean dirty) {
        try {
            this.dirty = dirty;
/*
            if (!origSourceConfig.equals(sourceConfig)) {
                this.dirty = true;
                return;
            }
*/
        } catch (Exception e) {
            log.error(e.getMessage(), e);

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
}
