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
import org.safehaus.penrose.directory.EntryClient;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.config.editor.ParametersPage;
import org.safehaus.penrose.studio.acl.editor.ACLPage;
import org.safehaus.penrose.studio.server.Server;

public class EntryEditor extends FormEditor implements ModifyListener {

    Logger log = Logger.getLogger(getClass());

    protected Server server;
    protected String partitionName;
    protected String entryId;

    EntryConfig origEntryConfig;
    protected EntryConfig entryConfig;

    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        EntryEditorInput ei = (EntryEditorInput)input;

        server = ei.getProject();
        partitionName = ei.getPartitionName();
        entryId = ei.getEntryId();

        try {
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            DirectoryClient directoryClient = partitionClient.getDirectoryClient();

            EntryClient entryClient = directoryClient.getEntryClient(entryId);
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
            addPage(new EntryPropertiesPage(this));
            addPage(new EntryLDAPPage(this));
            addPage(new EntrySourcesPage(this));
            addPage(new ACLPage(this, server, partitionName, entryConfig));

            ParametersPage parametersPage = new EntryParametersPage(this);
            parametersPage.setParameters(entryConfig.getParameters());
            addPage(parametersPage);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Composite getParent() {
        return getContainer();
    }

    public void showLDAPPage() {
        setActivePage(1);
    }

    public void showSourcesPage() {
        setActivePage(2);
    }

    public void showACLPage() {
        setActivePage(3);
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

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        DirectoryClient directoryClient = partitionClient.getDirectoryClient();
        directoryClient.updateEntry(origEntryConfig.getName(), entryConfig);
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
/*
            if (!origEntryConfig.equals(entryConfig)) {
                dirty = true;
                return;
            }
*/
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

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public EntryConfig getEntryConfig() {
        return entryConfig;
    }

    public void setEntryConfig(EntryConfig entryConfig) {
        this.entryConfig = entryConfig;
    }
}

