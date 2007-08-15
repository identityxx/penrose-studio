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

import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.util.FileUtil;
import org.safehaus.penrose.studio.partition.action.*;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class PartitionsNode extends Node {

    Logger log = Logger.getLogger(getClass());

    protected ServersView view;
    protected ProjectNode projectNode;

    protected Map<String,Node> partitions = new TreeMap<String,Node>();

    public PartitionsNode(ServersView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
        projectNode = (ProjectNode)parent;

        Project project = projectNode.getProject();
        for (PartitionConfig partitionConfig : project.getPartitionConfigs().getPartitionConfigs()) {
            addPartitionConfig(partitionConfig);
        }
    }

    public void showMenu(IMenuManager manager) {
        manager.add(new NewPartitionAction());
        manager.add(new ImportPartitionAction());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioWorkbenchAdvisor workbenchAdvisor = penroseStudio.getWorkbenchAdvisor();
        PenroseStudioWorkbenchWindowAdvisor workbenchWindowAdvisor = workbenchAdvisor.getWorkbenchWindowAdvisor();
        PenroseStudioActionBarAdvisor actionBarAdvisor = workbenchWindowAdvisor.getActionBarAdvisor();

        //if (actionBarAdvisor.getShowCommercialFeaturesAction().isChecked()) {
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            manager.add(new NewLDAPSnapshotPartitionAction());
            manager.add(new NewLDAPProxyPartitionAction());
        //}

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

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
/*
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new RefreshAction(this));
*/
    }

    public void refresh() throws Exception {
    }

    public void paste() throws Exception {

        Object object = view.getClipboard();
        if (!(object instanceof PartitionNode[])) return;

        Project project = projectNode.getProject();
        PartitionConfigs partitionConfigs = project.getPartitionConfigs();

        for (PartitionNode oldPartitionNode : (PartitionNode[])object) {

            PartitionConfig oldPartitionConfig = oldPartitionNode.getPartitionConfig();
            Project oldProject = oldPartitionNode.getProjectNode().getProject();

            String oldPartitionName = oldPartitionConfig.getName();
            String newPartitionName = oldPartitionName;

            while (partitionConfigs.getPartitionConfig(newPartitionName) != null) {

                PartitionDialog dialog = new PartitionDialog(view.getSite().getShell(), SWT.NONE);
                dialog.setName(newPartitionName);
                dialog.setText("New Partition Name");
                dialog.open();

                if (dialog.getAction() == PartitionDialog.CANCEL) return;

                newPartitionName = dialog.getName();
            }

            log.debug("Copying "+oldPartitionName+" partition into "+newPartitionName+".");

            File oldDir = new File(oldProject.getWorkDir(), "partitions"+File.separator+oldPartitionName);
            File newDir = new File(project.getWorkDir(), "partitions"+File.separator+newPartitionName);
            FileUtil.copy(oldDir, newDir);

            project.upload("partitions/"+newPartitionName);

            PartitionConfig newPartitionConfig = partitionConfigs.load(newDir);
            partitionConfigs.addPartitionConfig(newPartitionConfig);

            addPartitionConfig(newPartitionConfig);
        }

        view.setClipboard(null);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void addPartitionConfig(PartitionConfig partitionConfig) {

        PartitionNode partitionNode = new PartitionNode(
                partitionConfig.getName(),
                ServersView.PARTITION,
                PenrosePlugin.getImage(PenroseImage.PARTITION),
                partitionConfig,
                this
        );

        partitions.put(partitionConfig.getName(), partitionNode);
    }

    public void removePartitionConfig(String name) throws Exception {
        Project project = projectNode.getProject();
        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        partitionConfigs.removePartitionConfig(name);

        project.removeDirectory("partitions/"+name);

        partitions.remove(name);
    }

    public boolean hasChildren() throws Exception {
        return !partitions.isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {
        return partitions.values();
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }
}
