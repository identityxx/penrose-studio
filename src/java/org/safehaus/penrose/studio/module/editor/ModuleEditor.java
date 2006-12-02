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
package org.safehaus.penrose.studio.module.editor;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.partition.Partition;
import org.apache.log4j.Logger;

public class ModuleEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private Partition partition;

    ModuleConfig origModuleConfig;
    private ModuleConfig moduleConfig;

    private Collection mappings = new ArrayList();

    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
    }

    public void setInput(IEditorInput input) {
        super.setInput(input);

        ModuleEditorInput ei = (ModuleEditorInput)input;
        server = ei.getServer();
        partition = ei.getPartition();
        origModuleConfig = ei.getModuleConfig();
        moduleConfig = (ModuleConfig)origModuleConfig.clone();

        mappings.addAll(getPartition().getModuleMappings(getModuleConfig().getName()));

        setPartName(partition.getName()+" - "+moduleConfig.getName());
    }

    public void addPages() {
        try {
            addPage(new ModulePropertiesPage(this));
            addPage(new ModuleStatusPage(this));

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

            if (!origModuleConfig.equals(moduleConfig)) {
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
            ModuleEditorPage page = (ModuleEditorPage)i.next();
            page.refresh();
        }
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }

    public void setModuleConfig(ModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
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

        boolean rename = !origModuleConfig.getName().equals(moduleConfig.getName());
        if (rename) {
            partition.removeModuleConfig(origModuleConfig.getName());
        }

        origModuleConfig.copy(moduleConfig);

        if (rename) {
            partition.addModuleConfig(origModuleConfig);
        }

        partition.removeModuleMapping(moduleConfig.getName());

        for (Iterator i=mappings.iterator(); i.hasNext(); ) {
            ModuleMapping mapping = (ModuleMapping)i.next();
            mapping.setModuleName(moduleConfig.getName());
            partition.addModuleMapping(mapping);
        }

        setPartName(partition.getName()+" - "+moduleConfig.getName());

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

    public Collection getMappings() {
        return mappings;
    }

    public void setMappings(Collection mappings) {
        this.mappings = mappings;
    }
}
