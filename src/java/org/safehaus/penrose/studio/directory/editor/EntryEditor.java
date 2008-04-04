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
package org.safehaus.penrose.studio.directory.editor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.management.directory.EntryClient;
import org.safehaus.penrose.management.partition.PartitionClient;
import org.safehaus.penrose.management.partition.PartitionManagerClient;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.Project;

public class EntryEditor extends FormEditor implements ModifyListener {

    Logger log = Logger.getLogger(getClass());

    protected Project project;
    protected String partitionName;
    protected String entryId;

    EntryConfig origEntryConfig;
    protected EntryConfig entryConfig;

    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        EntryEditorInput ei = (EntryEditorInput)input;

        project = ei.getProject();
        partitionName = ei.getPartitionName();
        entryId = ei.getEntryId();

        try {
            PenroseClient client = project.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

            EntryClient entryClient = partitionClient.getEntryClient(entryId);
            origEntryConfig = entryClient.getEntryConfig();

            entryConfig = (EntryConfig) origEntryConfig.clone();

        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }

        String dn;
        if (entryConfig.getDn().isEmpty()) {
            dn = "Root DSE";
        } else {
            dn = entryConfig.getDn().toString();
        }
        setPartName(dn);
    }

    protected void addPages() {
        try {
            addPage(new LDAPPage(this));
            addPage(new SourcesPage(this));
            addPage(new ACLPage(this, project, partitionName, entryConfig));
            addPage(new MiscPage(this));
            //addPage(new EntryCachePage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
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
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void doSaveAs() {
    }

	public void store() throws Exception {
/*
        DirectoryConfig directoryConfig = partitionConfig.getDirectoryConfig();
        if (!origEntryConfig.getDn().matches(entryConfig.getDn())) {
            directoryConfig.renameEntryConfig(origEntryConfig, entryConfig.getDn());
        }

        origEntryConfig.copy(entryConfig);
*/
        //project.save(partitionConfig, directoryConfig);

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        partitionClient.updateEntry(origEntryConfig.getId(), entryConfig);
        partitionClient.store();

        String dn;
        if (entryConfig.getDn().isEmpty()) {
            dn = "Root DSE";
        } else {
            dn = entryConfig.getDn().toString();
        }

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

            if (!origEntryConfig.equals(entryConfig)) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }

    public void modifyText(ModifyEvent event) {
        checkDirty();
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public EntryConfig getEntryConfig() {
        return entryConfig;
    }

    public void setEntryConfig(EntryConfig entryConfig) {
        this.entryConfig = entryConfig;
    }
}

