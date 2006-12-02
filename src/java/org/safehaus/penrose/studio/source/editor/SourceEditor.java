package org.safehaus.penrose.studio.source.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.adapter.PenroseStudioAdapter;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.mapping.SourceMapping;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class SourceEditor extends FormEditor {

    public Logger log = Logger.getLogger(getClass());

    private Server server;
    private Partition partition;

    private SourceConfig originalSourceConfig;
    private SourceConfig sourceConfig;

    private boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
    }

    public void setInput(IEditorInput input) {
        super.setInput(input);

        SourceEditorInput ei = (SourceEditorInput)input;
        server = ei.getProject();
        partition = ei.getPartition();
        originalSourceConfig = ei.getSourceConfig();
        sourceConfig = (SourceConfig)getOriginalSourceConfig().clone();

        setPartName("["+server.getName()+"] "+partition.getName()+" - "+getSourceConfig().getName());
    }

    public void addPages() {
        try {
            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            ConnectionConfig connectionConfig = partition.getConnectionConfig(sourceConfig.getConnectionName());

            if (connectionConfig != null) {
                PenroseStudioAdapter adapter = penroseStudio.getAdapter(connectionConfig.getAdapterName());

                if (adapter != null) {
                    Collection pages = adapter.createSourceEditorPages(this);

                    for (Iterator i=pages.iterator(); i.hasNext(); ) {
                        SourceEditorPage page = (SourceEditorPage)i.next();
                        addPage(page);
                    }
                }
            }

            addPage(new SourceCachePage(this));
            addPage(new SourceAdvancedPage(this));
            addPage(new SourceStatusPage(this));

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public SourceConfig getOriginalSourceConfig() {
        return originalSourceConfig;
    }

    public void setOriginalSourceConfig(SourceConfig originalSourceConfig) {
        this.originalSourceConfig = originalSourceConfig;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void checkDirty() {
        try {
            dirty = false;

            if (!originalSourceConfig.equals(sourceConfig)) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.debug(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
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

    public void store() throws Exception {

        if (!originalSourceConfig.getName().equals(sourceConfig.getName())) {
            partition.renameSourceConfig(originalSourceConfig, sourceConfig.getName());

            for (Iterator i=partition.getEntryMappings().iterator(); i.hasNext(); ) {
                EntryMapping entryMapping = (EntryMapping)i.next();
                for (Iterator j=entryMapping.getSourceMappings().iterator(); j.hasNext(); ) {
                    SourceMapping sourceMapping = (SourceMapping)j.next();
                    if (!sourceMapping.getSourceName().equals(originalSourceConfig.getName())) continue;
                    sourceMapping.setSourceName(sourceConfig.getName());
                }
            }
        }

        getPartition().modifySourceConfig(sourceConfig.getName(), sourceConfig);

        setPartName("["+server.getName()+"] "+partition.getName()+" - "+getSourceConfig().getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();

        checkDirty();
    }

    public void doSaveAs() {
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
