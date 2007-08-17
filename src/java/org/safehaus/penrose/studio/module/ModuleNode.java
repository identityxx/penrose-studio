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

import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.partition.PartitionNode;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.module.editor.ModuleEditorInput;
import org.safehaus.penrose.studio.module.editor.ModuleEditor;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.module.ModuleConfigs;
import org.safehaus.penrose.partition.PartitionConfig;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.swt.graphics.Image;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class ModuleNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ServersView serversView;
    private ProjectNode projectNode;
    private PartitionsNode partitionsNode;
    private PartitionNode partitionNode;
    private ModulesNode modulesNode;

    private PartitionConfig partitionConfig;
    private ModuleConfig moduleConfig;

    public ModuleNode(String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);

        modulesNode = (ModulesNode)parent;
        partitionNode = modulesNode.getPartitionNode();
        partitionsNode = partitionNode.getPartitionsNode();
        projectNode = partitionsNode.getProjectNode();
        serversView = projectNode.getServersView();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Copy") {
            public void run() {
                try {
                    copy();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.SIZE_16x16, PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
	}

    public void open() throws Exception {

        ModuleEditorInput ei = new ModuleEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setPartitionConfig(partitionConfig);
        ei.setModuleConfig(moduleConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, ModuleEditor.class.getName());
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                serversView.getSite().getShell(),
                "Confirmation", "Remove Module \""+moduleConfig.getName()+"\"?"
        );

        if (!confirm) return;

        ModuleConfigs moduleConfigs = partitionConfig.getModuleConfigs();

        moduleConfigs.removeModuleMapping(moduleConfig.getName());
        moduleConfigs.removeModuleConfig(moduleConfig.getName());

        Project project = projectNode.getProject();
        project.save(partitionConfig, moduleConfigs);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void copy() throws Exception {
        serversView.setClipboard(moduleConfig);
    }

    public void paste() throws Exception {

        Object newObject = serversView.getClipboard();

        if (!(newObject instanceof ModuleConfig)) return;

        ModuleConfigs moduleConfigs = partitionConfig.getModuleConfigs();

        ModuleConfig newModuleConfig = (ModuleConfig)((ModuleConfig)newObject).clone();
        String oldName = newModuleConfig.getName();

        int counter = 1;
        String name = oldName;
        while (moduleConfigs.getModuleConfig(name) != null) {
            counter++;
            name = oldName+" ("+counter+")";
        }

        newModuleConfig.setName(name);
        moduleConfigs.addModuleConfig(newModuleConfig);

        Collection<ModuleMapping> mappings = moduleConfigs.getModuleMappings(oldName);
        if (mappings != null) {
            for (ModuleMapping mapping : mappings) {
                ModuleMapping newMapping = (ModuleMapping) ((ModuleMapping) mapping).clone();
                newMapping.setModuleName(name);
                newMapping.setModuleConfig(newModuleConfig);
                moduleConfigs.addModuleMapping(newMapping);
            }
        }

        serversView.setClipboard(null);

        Project project = projectNode.getProject();
        project.save(partitionConfig, moduleConfigs);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }

    public void setModuleConfig(ModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }

    public ServersView getServersView() {
        return serversView;
    }

    public void setServersView(ServersView serversView) {
        this.serversView = serversView;
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }

    public PartitionsNode getPartitionsNode() {
        return partitionsNode;
    }

    public void setPartitionsNode(PartitionsNode partitionsNode) {
        this.partitionsNode = partitionsNode;
    }

    public PartitionNode getPartitionNode() {
        return partitionNode;
    }

    public void setPartitionNode(PartitionNode partitionNode) {
        this.partitionNode = partitionNode;
    }

    public ModulesNode getModulesNode() {
        return modulesNode;
    }

    public void setModulesNode(ModulesNode modulesNode) {
        this.modulesNode = modulesNode;
    }
}
