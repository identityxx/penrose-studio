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
package org.safehaus.penrose.studio.connection.editor;

import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseWorkbenchAdvisor;
import org.safehaus.penrose.studio.PenroseWorkbenchWindowAdvisor;
import org.safehaus.penrose.studio.PenroseActionBarAdvisor;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditor;
import org.safehaus.penrose.studio.connection.editor.JDBCConnectionPropertiesPage;
import org.safehaus.penrose.studio.connection.editor.JDBCConnectionTablesPage;
import org.safehaus.penrose.partition.SourceConfig;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class JDBCConnectionEditor extends ConnectionEditor {

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);

        setPartName(getPartition().getName()+"/"+getConnectionConfig().getName());
    }

    public void addPages() {
        try {
            addPage(new JDBCConnectionPropertiesPage(this));

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            PenroseWorkbenchAdvisor workbenchAdvisor = penroseStudio.getWorkbenchAdvisor();
            PenroseWorkbenchWindowAdvisor workbenchWindowAdvisor = workbenchAdvisor.getWorkbenchWindowAdvisor();
            PenroseActionBarAdvisor actionBarAdvisor = workbenchWindowAdvisor.getActionBarAdvisor();

            //if (actionBarAdvisor.getShowCommercialFeaturesAction().isChecked()) {
                addPage(new JDBCConnectionTablesPage(this));
            //}

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

        if (!getOriginalConnectionConfig().getName().equals(getConnectionConfig().getName())) {
            getPartition().renameConnectionConfig(getOriginalConnectionConfig(), getConnectionConfig().getName());

            for (Iterator i=getPartition().getSourceConfigs().iterator(); i.hasNext(); ) {
                SourceConfig sourceConfig = (SourceConfig)i.next();
                if (!sourceConfig.getConnectionName().equals(getOriginalConnectionConfig().getName())) continue;
                sourceConfig.setConnectionName(getConnectionConfig().getName());
            }
        }

        getPartition().modifyConnectionConfig(getConnectionConfig().getName(), getConnectionConfig());

        setPartName(getPartition().getName()+"/"+getConnectionConfig().getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();

        checkDirty();
    }

    public boolean isSaveAsAllowed() {
        return false;
    }
}