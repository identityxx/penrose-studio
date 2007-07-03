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
package org.safehaus.penrose.studio.module;

import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.module.action.NewModuleAction;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.partition.Partition;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.swt.graphics.Image;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class ModulesNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    private Partition partition;

    public ModulesNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new NewModuleAction(this));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
    }

    public void paste() throws Exception {

        Object newObject = view.getClipboard();

        if (!(newObject instanceof ModuleConfig)) return;

        ModuleConfig newModuleConfig = (ModuleConfig)((ModuleConfig)newObject).clone();
        String oldName = newModuleConfig.getName();

        int counter = 1;
        String name = oldName;
        while (partition.getModuleConfig(name) != null) {
            counter++;
            name = oldName+" ("+counter+")";
        }

        newModuleConfig.setName(name);
        partition.addModuleConfig(newModuleConfig);

        Collection mappings = partition.getModuleMappings(oldName);
        if (mappings != null) {
            for (Iterator i=mappings.iterator(); i.hasNext(); ) {
                ModuleMapping mapping = (ModuleMapping)((ModuleMapping)i.next()).clone();
                mapping.setModuleName(name);
                mapping.setModuleConfig(newModuleConfig);
                partition.addModuleMapping(mapping);
            }
        }

        view.setClipboard(null);

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        penroseApplication.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        return !partition.getModuleConfigs().isEmpty();
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        Collection modules = partition.getModuleConfigs();
        for (Iterator i=modules.iterator(); i.hasNext(); ) {
            ModuleConfig moduleConfig = (ModuleConfig)i.next();

            ModuleNode moduleNode = new ModuleNode(
                    view,
                    moduleConfig.getName(),
                    ObjectsView.MODULE,
                    PenrosePlugin.getImage(PenroseImage.MODULE),
                    moduleConfig,
                    this
            );

            moduleNode.setPartition(partition);
            moduleNode.setModuleConfig(moduleConfig);

            children.add(moduleNode);
        }

        return children;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
