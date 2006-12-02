package org.safehaus.penrose.studio.partition.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.partition.PartitionConfig;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class PartitionEditor extends FormEditor {

    public Logger log = Logger.getLogger(getClass());

    private Server server;
    private PartitionConfig partitionConfig;
    private PartitionConfig originalPartitionConfig;

    private boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
    }

    public void setInput(IEditorInput input) {
        super.setInput(input);

        PartitionEditorInput ei = (PartitionEditorInput)input;
        server = ei.getServer();
        originalPartitionConfig = ei.getPartitionConfig();
        partitionConfig = (PartitionConfig)originalPartitionConfig.clone();

        setPartName("["+server.getName()+"] "+partitionConfig.getName());
    }

    public void addPages() {
        try {
            addPage(new PartitionPropertiesPage(this));
            addPage(new PartitionCachePage(this, "ENTRY_CACHE", "  Entry Cache  ", partitionConfig.getEntryCacheConfig()));
            addPage(new PartitionCachePage(this, "SOURCE_CACHE", "  Source Cache  ", partitionConfig.getSourceCacheConfig()));
            addPage(new PartitionStatusPage(this));

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void checkDirty() {
        try {
            dirty = false;

            if (!originalPartitionConfig.equals(partitionConfig)) {
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
            PartitionEditorPage page = (PartitionEditorPage)i.next();
            page.refresh();
        }
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

        originalPartitionConfig.copy(partitionConfig);

        setPartName("["+server.getName()+"] "+partitionConfig.getName());

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

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }
}
