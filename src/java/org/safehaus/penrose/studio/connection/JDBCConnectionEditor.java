/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.studio.connection;

import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.partition.ConnectionConfig;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenroseWorkbenchAdvisor;
import org.safehaus.penrose.studio.PenroseWorkbenchWindowAdvisor;
import org.safehaus.penrose.studio.PenroseActionBarAdvisor;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.SourceConfig;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class JDBCConnectionEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());
    
    boolean dirty;

    private Partition partition;
    ConnectionConfig origConnectionConfig;
    private ConnectionConfig connectionConfig;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        JDBCConnectionEditorInput cei = (JDBCConnectionEditorInput)input;

        partition = cei.getPartition();
        origConnectionConfig = cei.getConnectionConfig();
        connectionConfig = (ConnectionConfig)origConnectionConfig.clone();

        setSite(site);
        setInput(input);
        setPartName(partition.getName()+"/"+connectionConfig.getName());
    }

    public void addPages() {
        try {
            addPage(new JDBCConnectionPropertiesPage(this));

            PenroseApplication penroseApplication = PenroseApplication.getInstance();
            PenroseWorkbenchAdvisor workbenchAdvisor = penroseApplication.getWorkbenchAdvisor();
            PenroseWorkbenchWindowAdvisor workbenchWindowAdvisor = workbenchAdvisor.getWorkbenchWindowAdvisor();
            PenroseActionBarAdvisor actionBarAdvisor = workbenchWindowAdvisor.getActionBarAdvisor();

            if (actionBarAdvisor.getShowCommercialFeaturesAction().isChecked()) {

                addPage(new JDBCConnectionTablesPage(this));
            }

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
            partition.renameConnectionConfig(origConnectionConfig, connectionConfig.getName());

            for (Iterator i=partition.getSourceConfigs().iterator(); i.hasNext(); ) {
                SourceConfig sourceConfig = (SourceConfig)i.next();
                if (!sourceConfig.getConnectionName().equals(origConnectionConfig.getName())) continue;
                sourceConfig.setConnectionName(connectionConfig.getName());
            }
        }

        partition.modifyConnectionConfig(connectionConfig.getName(), connectionConfig);

        setPartName(partition.getName()+"/"+connectionConfig.getName());

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

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }
}