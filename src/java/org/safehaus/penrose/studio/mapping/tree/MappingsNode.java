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
package org.safehaus.penrose.studio.mapping.tree;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.mapping.MappingConfig;
import org.safehaus.penrose.mapping.MappingManagerClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.action.RefreshAction;
import org.safehaus.penrose.studio.mapping.action.ImportMappingsAction;
import org.safehaus.penrose.studio.mapping.action.NewMappingAction;
import org.safehaus.penrose.studio.mapping.dnd.MappingTransfer;
import org.safehaus.penrose.studio.partition.tree.PartitionNode;
import org.safehaus.penrose.studio.partition.tree.PartitionsNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class MappingsNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ServersView serversView;
    private ServerNode serverNode;
    private PartitionsNode partitionsNode;
    private PartitionNode partitionNode;

    private String partitionName;

    public MappingsNode(String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
        partitionNode = (PartitionNode)parent;
        partitionsNode = partitionNode.getPartitionsNode();
        serverNode = partitionsNode.getServerNode();
        serversView = serverNode.getServersView();
    }

    public void update() throws Exception {

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        MappingManagerClient mappingManagerClient = partitionClient.getMappingManagerClient();

        for (String mappingName : mappingManagerClient.getMappingNames()) {

            MappingNode mappingNode = new MappingNode(
                    mappingName,
                    PenroseStudio.getImage(PenroseImage.MAPPING),
                    mappingName,
                    this
            );

            mappingNode.setPartitionName(partitionName);
            mappingNode.setMappingName(mappingName);

            addChild(mappingNode);
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

        manager.add(new NewMappingAction(this));
        manager.add(new ImportMappingsAction(this));

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
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new RefreshAction(this));
    }

    public void paste() throws Exception {

        log.debug("Pasting mappings:");

        MappingConfig[] mappingConfigs = (MappingConfig[]) serversView.getSWTClipboard().getContents(MappingTransfer.getInstance());
        if (mappingConfigs == null) return;

        Server server = serverNode.getServer();

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        MappingManagerClient mappingManagerClient = partitionClient.getMappingManagerClient();

        Collection<String> names = mappingManagerClient.getMappingNames();

        for (MappingConfig mappingConfig : mappingConfigs) {
            String name = mappingConfig.getName();

            int counter = 1;
            String newName = name;
            while (names.contains(newName)) {
                counter++;
                newName = name+"_"+counter;
            }

            log.debug(" - "+name+" -> "+newName);
            mappingConfig.setName(newName);

            mappingManagerClient.createMapping(mappingConfig);
        }

        partitionClient.store();

        ServersView serversView = ServersView.getInstance();
        serversView.refresh(this);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
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

    public ServerNode getServerNode() {
        return serverNode;
    }

    public void setServerNode(ServerNode serverNode) {
        this.serverNode = serverNode;
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