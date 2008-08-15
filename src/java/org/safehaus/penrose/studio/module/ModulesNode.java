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

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.safehaus.penrose.module.ModuleClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.module.action.NewModuleAction;
import org.safehaus.penrose.studio.partition.PartitionNode;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class ModulesNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ServersView serversView;
    private ProjectNode projectNode;
    private PartitionsNode partitionsNode;
    private PartitionNode partitionNode;

    private String partitionName;

    public ModulesNode(String name, Image image, Object object, Object parent) {
        super(name, image, object, parent);

        partitionNode = (PartitionNode)parent;
        partitionsNode = partitionNode.getPartitionsNode();
        projectNode = partitionsNode.getProjectNode();
        serversView = projectNode.getServersView();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new NewModuleAction(this));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void paste() throws Exception {

        Object newObject = serversView.getClipboard();

        if (!(newObject instanceof ModuleConfig)) return;

        Project project = projectNode.getProject();

        ModuleConfig newModuleConfig = (ModuleConfig)((ModuleConfig)newObject).clone();
        String oldName = newModuleConfig.getName();
        serversView.setClipboard(null);
/*
        ModuleConfigManager moduleConfigManager = partitionConfig.getModuleConfigManager();
        int counter = 1;
        String name = oldName;
        while (moduleConfigManager.getModuleConfig(name) != null) {
            counter++;
            name = oldName+" ("+counter+")";
        }
        newModuleConfig.setName(name);

        moduleConfigManager.addModuleConfig(newModuleConfig);

        Collection<ModuleMapping> mappings = moduleConfigManager.getModuleMappings(oldName);
        if (mappings != null) {
            for (ModuleMapping mapping : mappings) {
                ModuleMapping newMapping = (ModuleMapping) ((ModuleMapping) mapping).clone();
                newMapping.setModuleName(name);
                newMapping.setModuleConfig(newModuleConfig);
                moduleConfigManager.addModuleMapping(newMapping);
            }
        }

        project.save(partitionConfig, moduleConfigManager);
*/
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        Collection<String> moduleNames = partitionClient.getModuleNames();

        int counter = 1;
        String name = oldName;
        while (moduleNames.contains(name)) {
            counter++;
            name = oldName+" ("+counter+")";
        }
        newModuleConfig.setName(name);

        ModuleClient moduleClient = partitionClient.getModuleClient(oldName);
        Collection<ModuleMapping> moduleMappings = moduleClient.getModuleMappings();

        if (moduleMappings != null) {
            for (ModuleMapping moduleMapping : moduleMappings) {
                moduleMapping.setModuleName(name);
            }
        }

        partitionClient.createModule(newModuleConfig, moduleMappings);

        partitionClient.store();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        return !getChildren().isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        Project project = projectNode.getProject();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        Collection<String> moduleNames = partitionClient.getModuleNames();
        for (String moduleName : moduleNames) {

            ModuleNode moduleNode = new ModuleNode(
                    moduleName,
                    PenroseStudioPlugin.getImage(PenroseImage.MODULE),
                    moduleName,
                    this
            );

            moduleNode.setPartitionName(partitionName);
            moduleNode.setModuleName(moduleName);

            children.add(moduleNode);
        }

        return children;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
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
}
