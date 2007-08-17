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
package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.mapping.*;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.directory.DirectoryConfigs;
import org.apache.log4j.Logger;

public class MappingEditor extends FormEditor implements ModifyListener {

    Logger log = Logger.getLogger(getClass());

    protected Project project;
    protected PartitionConfig partitionConfig;
	protected EntryMapping entry;
    EntryMapping origEntry;

    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        MappingEditorInput mei = (MappingEditorInput)input;

        project = mei.getProject();
        partitionConfig = mei.getPartitionConfig();
        origEntry = mei.getEntryMapping();

        try {
            entry = (EntryMapping)origEntry.clone();
        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }

        setSite(site);
        setInput(input);

        String dn;
        if (entry.getDn().isEmpty()) {
            dn = "Root DSE";
        } else {
            dn = entry.getDn().toString();
        }
        setPartName(dn);
    }

    protected void addPages() {
        try {
            addPage(new LDAPPage(this));
            addPage(new SourcesPage(this));
            addPage(new ACLPage(this));
            addPage(new MiscPage(this));
            //addPage(new EntryCachePage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public Composite getParent() {
        return getContainer();
    }

    public void showSourcesPage() {
        setActivePage(1);
    }

    public void showACLPage() {
        setActivePage(2);
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

        DirectoryConfigs directoryConfigs = partitionConfig.getDirectoryConfigs();
        if (!origEntry.getDn().matches(entry.getDn())) {
            directoryConfigs.renameEntryMapping(origEntry, entry.getDn());
        }

        origEntry.copy(entry);

        String dn;
        if (entry.getDn().isEmpty()) {
            dn = "Root DSE";
        } else {
            dn = entry.getDn().toString();
        }

        project.save(partitionConfig, directoryConfigs);

        setPartName(dn);

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

            if (!origEntry.equals(entry)) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }

    public void modifyText(ModifyEvent event) {
        checkDirty();
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public EntryMapping getEntry() {
        return entry;
    }

    public void setEntry(EntryMapping entry) {
        this.entry = entry;
    }
}

