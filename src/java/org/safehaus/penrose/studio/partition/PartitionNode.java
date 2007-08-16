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
package org.safehaus.penrose.studio.partition;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Image;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.partition.action.ExportPartitionAction;
import org.safehaus.penrose.studio.connection.ConnectionsNode;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.module.ModulesNode;
import org.safehaus.penrose.studio.source.SourcesNode;
import org.safehaus.penrose.studio.directory.DirectoryNode;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class PartitionNode extends Node {

    public Logger log = Logger.getLogger(getClass());

    private ServersView view;
    private ProjectNode projectNode;
    private PartitionsNode partitionsNode;

    private PartitionConfig partitionConfig;

    Action copyAction;

    Collection<Node> children = new ArrayList<Node>();

    public PartitionNode(String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);

        partitionConfig = (PartitionConfig)object;

        partitionsNode = (PartitionsNode)parent;
        projectNode = partitionsNode.getProjectNode();
        view = projectNode.getServersView();

        DirectoryNode directoryNode = new DirectoryNode(
                ServersView.DIRECTORY,
                ServersView.DIRECTORY,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ServersView.DIRECTORY,
                this
        );

        directoryNode.setPartitionConfig(partitionConfig);

        children.add(directoryNode);

        ConnectionsNode connectionsNode = new ConnectionsNode(
                ServersView.CONNECTIONS,
                ServersView.CONNECTIONS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                partitionConfig,
                this
        );

        connectionsNode.setPartitionConfig(partitionConfig);

        children.add(connectionsNode);

        SourcesNode sourcesNode = new SourcesNode(
                ServersView.SOURCES,
                ServersView.SOURCES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ServersView.SOURCES,
                this
        );

        sourcesNode.setPartitionConfig(partitionConfig);

        children.add(sourcesNode);

        ModulesNode modulesNode = new ModulesNode(
                view,
                ServersView.MODULES,
                ServersView.MODULES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ServersView.MODULES,
                this
        );

        modulesNode.setPartitionConfig(partitionConfig);

        children.add(modulesNode);
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Save") {
            public void run() {
                try {
                    save();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Upload") {
            public void run() {
                try {
                    upload();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new ExportPartitionAction(this));

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
            public boolean isEnabled() {
                Object object = view.getClipboard();
                return object != null && object instanceof PartitionNode[];
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

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

    public void save() throws Exception {
        log.debug("Saving "+name+" partition.");

        Project project = projectNode.getProject();

        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        File workDir = project.getWorkDir();

        partitionConfigs.store(workDir, partitionConfig);
    }

    public void upload() throws Exception {
        log.debug("Uploading "+name+" partition.");

        Project project = projectNode.getProject();
        project.upload("partitions/"+name);
    }

    public void remove() throws Exception {

        Shell shell = view.getSite().getShell();

        boolean confirm = MessageDialog.openQuestion(
                shell,
                "Confirmation",
                "Remove Partition \""+partitionConfig.getName()+"\"?");

        if (!confirm) return;

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof PartitionNode)) continue;

            PartitionConfig partitionConfig = ((PartitionNode)node).getPartitionConfig();
            partitionsNode.removePartitionConfig(partitionConfig.getName());
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void copy() throws Exception {
        log.debug("Copying "+name+" partition.");

        Collection<PartitionNode> nodes = new ArrayList<PartitionNode>();
        for (Node node : view.getSelectedNodes()) {
            nodes.add((PartitionNode)node);
        }
        view.setClipboard(nodes.toArray(new PartitionNode[nodes.size()]));
    }

    public void paste() throws Exception {
        PartitionsNode partitionsNode = (PartitionsNode)parent;
        partitionsNode.paste();
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {
        return children;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public ServersView getView() {
        return view;
    }

    public void setView(ServersView view) {
        this.view = view;
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
}
