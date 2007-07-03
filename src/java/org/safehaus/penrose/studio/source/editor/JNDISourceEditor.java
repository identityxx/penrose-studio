/**
 * Copyright (c) 2000-2006, Identyx Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.safehaus.penrose.studio.source.editor;

import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.mapping.SourceMapping;
import org.safehaus.penrose.source.Sources;
import org.apache.log4j.Logger;

import java.util.Iterator;

public class JNDISourceEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());

    Partition partition;
	SourceConfig sourceConfig;
    SourceConfig origSourceConfig;

    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {

        try {
            JNDISourceEditorInput ei = (JNDISourceEditorInput)input;
            partition = ei.getPartition();
            origSourceConfig = ei.getSourceConfig();
            sourceConfig = (SourceConfig)origSourceConfig.clone();

            setSite(site);
            setInput(input);
            setPartName(partition.getName()+"/"+sourceConfig.getName());
            
        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }
    }

    public void addPages() {
        try {
            addPage(new JNDISourcePropertyPage(this));
            addPage(new JNDISourceBrowsePage(this));
            //addPage(new JNDISourceCachePage(this));

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

        Sources sources = partition.getSources();
        if (!origSourceConfig.getName().equals(sourceConfig.getName())) {
            sources.renameSourceConfig(origSourceConfig, sourceConfig.getName());

            for (Iterator i=partition.getEntryMappings().iterator(); i.hasNext(); ) {
                EntryMapping entryMapping = (EntryMapping)i.next();
                for (Iterator j=entryMapping.getSourceMappings().iterator(); j.hasNext(); ) {
                    SourceMapping sourceMapping = (SourceMapping)j.next();
                    if (!sourceMapping.getSourceName().equals(origSourceConfig.getName())) continue;
                    sourceMapping.setSourceName(sourceConfig.getName());
                }
            }
        }

        sources.modifySourceConfig(sourceConfig.getName(), sourceConfig);

        setPartName(partition.getName()+"/"+sourceConfig.getName());

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        penroseApplication.notifyChangeListeners();

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

            if (!origSourceConfig.equals(sourceConfig)) {
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
}
