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
package org.safehaus.penrose.studio.directory.node;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntryClient;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.schema.action.NewSchemaEntryAction;
import org.safehaus.penrose.studio.schema.action.NewADSchemaProxyAction;
import org.safehaus.penrose.studio.rootDse.action.NewRootDSEAction;
import org.safehaus.penrose.studio.rootDse.action.NewRootDSEProxyAction;
import org.safehaus.penrose.studio.directory.action.*;
import org.safehaus.penrose.studio.partition.node.PartitionNode;
import org.safehaus.penrose.studio.partition.node.PartitionsNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.node.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class DirectoryNode extends Node {

    Logger log = Logger.getLogger(getClass());

    protected ServersView view;
    protected ServerNode serverNode;

    private String partitionName;

    public DirectoryNode(String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
        PartitionNode partitionNode = (PartitionNode)parent;
        PartitionsNode partitionsNode = partitionNode.getPartitionsNode();
        serverNode = partitionsNode.getProjectNode();
        view = serverNode.getServersView();
    }

    public void init() throws Exception {
        updateChildren();
    }

    public void updateChildren() throws Exception {
        Server project = serverNode.getServer();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        DirectoryClient directoryClient = partitionClient.getDirectoryClient();

        for (String id : directoryClient.getRootEntryNames()) {
            EntryClient entryClient = directoryClient.getEntryClient(id);
            EntryConfig entryConfig = entryClient.getEntryConfig();

            String label;
            if (entryConfig.getDn().isEmpty()) {
                label = "Root DSE";
            } else {
                label = entryConfig.getDn().toString();
            }

            EntryNode entryNode = new EntryNode(
                    label,
                    PenroseStudio.getImage(PenroseImage.HOME_NODE),
                    entryConfig,
                    this
            );

            entryNode.setPartitionName(partitionName);
            entryNode.setEntryConfig(entryConfig);
            entryNode.init();
            
            children.add(entryNode);
        }
    }

    public void refresh() throws Exception {

        children.clear();
        updateChildren();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new NewRootEntryAction(this));
        manager.add(new NewRootProxyAction(this));
        //manager.add(new CreateLDAPSnapshotEntryAction(this));

        if ("DEFAULT".equals(partitionName)) {
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

            manager.add(new NewRootDSEAction(this));
            manager.add(new NewRootDSEProxyAction(this));
        }

        if ("DEFAULT".equals(partitionName)) {
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

            manager.add(new NewSchemaEntryAction(this));
            manager.add(new NewADSchemaProxyAction(this));
        }

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Refresh") {
            public void run() {
                try {
                    refresh();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public ServersView getView() {
        return view;
    }

    public void setView(ServersView view) {
        this.view = view;
    }

    public ServerNode getServerNode() {
        return serverNode;
    }

    public void setServerNode(ServerNode serverNode) {
        this.serverNode = serverNode;
    }
}
