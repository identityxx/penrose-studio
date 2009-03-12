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
package org.safehaus.penrose.studio.partition.tree;

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
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.action.RefreshAction;
import org.safehaus.penrose.studio.partition.action.*;
import org.safehaus.penrose.studio.partition.dialog.PartitionDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class PartitionsNode extends Node {

    Logger log = Logger.getLogger(getClass());

    protected ServersView view;
    protected ServerNode serverNode;

    public PartitionsNode(ServersView view, String name, ServerNode serverNode) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, serverNode);
        this.view = view;
        this.serverNode = serverNode;
    }

    public void update() throws Exception {

        PartitionNode rootPartitionName = new PartitionNode(
                PartitionConfig.ROOT,
                PartitionConfig.ROOT,
                this
        );
        rootPartitionName.init();

        addChild(rootPartitionName);

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

        for (String partitionName : partitionManagerClient.getPartitionNames()) {

            PartitionNode partitionNode = new PartitionNode(
                    partitionName,
                    partitionName,
                    this
            );
            partitionNode.init();

            addChild(partitionNode);
        }
    }

    public void expand() throws Exception {
        if (children == null) update();
    }

    public void refresh() throws Exception {
        removeChildren();
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
                    ErrorDialog.open(e);
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

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

        Collection<String> partitionNames = partitionManagerClient.getPartitionNames();
        //PartitionConfigManager partitionConfigManager = project.getPartitionConfigManager();

        for (PartitionNode oldPartitionNode : (PartitionNode[])object) {

            Server oldProject = oldPartitionNode.getServerNode().getServer();
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
    }

    public ServerNode getServerNode() {
        return serverNode;
    }

    public void setServerNode(ServerNode serverNode) {
        this.serverNode = serverNode;
    }
}
