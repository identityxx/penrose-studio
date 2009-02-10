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
package org.safehaus.penrose.studio.partition.node;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.partition.action.*;
import org.safehaus.penrose.studio.partition.dialog.PartitionDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.node.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class PartitionsNode extends Node {

    Logger log = Logger.getLogger(getClass());

    protected ServersView view;
    protected ServerNode projectNode;

    public PartitionsNode(ServersView view, String name, Node parent) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, parent);
        this.view = view;
        projectNode = (ServerNode)parent;
    }

    public void init() throws Exception {
        update();
    }

    public void update() throws Exception {

        PartitionNode defaultPartitionName = new PartitionNode(
                "DEFAULT",
                PenroseStudio.getImage(PenroseImage.PARTITION),
                "DEFAULT",
                this
        );
        defaultPartitionName.init();

        children.add(defaultPartitionName);

        Server project = projectNode.getServer();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

        for (String partitionName : partitionManagerClient.getPartitionNames()) {

            PartitionNode partitionNode = new PartitionNode(
                    partitionName,
                    PenroseStudio.getImage(PenroseImage.PARTITION),
                    partitionName,
                    this
            );
            partitionNode.init();

            children.add(partitionNode);
        }
    }

    public void refresh() throws Exception {
        children.clear();
        update();
    }

    public void showMenu(IMenuManager manager) {
        manager.add(new NewPartitionAction());
        manager.add(new ImportPartitionAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            public boolean isEnabled() {
                Object object = view.getClipboard();
                return object != null && object instanceof PartitionNode[];
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new RefreshAction(this));

    }

    public void paste() throws Exception {

        Object object = view.getClipboard();
        if (!(object instanceof PartitionNode[])) return;

        view.setClipboard(null);

        Server project = projectNode.getServer();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

        Collection<String> partitionNames = partitionManagerClient.getPartitionNames();
        //PartitionConfigManager partitionConfigManager = project.getPartitionConfigManager();

        for (PartitionNode oldPartitionNode : (PartitionNode[])object) {

            Server oldProject = oldPartitionNode.getProjectNode().getServer();
            PenroseClient oldClient = oldProject.getClient();
            PartitionManagerClient oldPartitionManagerClient = oldClient.getPartitionManagerClient();

            String oldPartitionName = oldPartitionNode.getPartitionName();
            PartitionClient oldPartitionClient = oldPartitionManagerClient.getPartitionClient(oldPartitionName);
            PartitionConfig oldPartitionConfig = oldPartitionClient.getPartitionConfig();

            String newPartitionName = oldPartitionName;

            while (partitionNames.contains(newPartitionName)) {

                PartitionDialog dialog = new PartitionDialog(view.getSite().getShell(), SWT.NONE);
                dialog.setName(newPartitionName);
                dialog.setText("New Partition Name");
                dialog.open();

                if (dialog.getAction() == PartitionDialog.CANCEL) return;

                newPartitionName = dialog.getName();
            }

            PartitionConfig newPartitionConfig = (PartitionConfig)oldPartitionConfig.clone();
            newPartitionConfig.setName(newPartitionName);

            //log.debug("Copying "+oldPartitionName+" partition into "+newPartitionName+".");

            //File oldDir = new File(oldProject.getWorkDir(), "partitions"+File.separator+oldPartitionName);
            //File newDir = new File(project.getWorkDir(), "partitions"+File.separator+newPartitionName);
            //FileUtil.copy(oldDir, newDir);

            //project.upload("partitions/"+newPartitionName);

            //PartitionConfig newPartitionConfig = partitionConfigManager.load(newDir);
            //partitionConfigManager.addPartitionConfig(newPartitionConfig);

            //addPartitionConfig(newPartitionConfig);

            partitionManagerClient.addPartition(newPartitionConfig);
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public ServerNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ServerNode projectNode) {
        this.projectNode = projectNode;
    }
}
