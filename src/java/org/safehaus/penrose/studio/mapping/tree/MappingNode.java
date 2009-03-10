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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.mapping.MappingClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.mapping.MappingConfig;
import org.safehaus.penrose.mapping.MappingManagerClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.mapping.editor.MappingEditor;
import org.safehaus.penrose.studio.mapping.editor.MappingEditorInput;
import org.safehaus.penrose.studio.mapping.dnd.MappingTransfer;
import org.safehaus.penrose.studio.partition.tree.PartitionNode;
import org.safehaus.penrose.studio.partition.tree.PartitionsNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class MappingNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ServersView serversView;
    private ServerNode serverNode;
    private PartitionsNode partitionsNode;
    private PartitionNode partitionNode;
    private MappingsNode mappingsNode;

    private String partitionName;
    private String mappingName;

    public MappingNode(String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
        mappingsNode = (MappingsNode)parent;
        partitionNode = mappingsNode.getPartitionNode();
        partitionsNode = partitionNode.getPartitionsNode();
        serverNode = partitionsNode.getServerNode();
        this.serversView = serverNode.getServersView();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
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
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    mappingsNode.paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Delete", PenroseStudio.getImageDescriptor(PenroseImage.DELETE_SMALL)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        MappingEditorInput ei = new MappingEditorInput();
        ei.setServer(serverNode.getServer());
        ei.setPartitionName(partitionName);
        ei.setMappingName(mappingName);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, MappingEditor.class.getName());
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                serversView.getSite().getShell(),
                "Confirmation","Remove selected mappings?");

        if (!confirm) return;

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        MappingManagerClient mappingManagerClient = partitionClient.getMappingManagerClient();

        for (Node node : serversView.getSelectedNodes()) {
            if (!(node instanceof MappingNode)) continue;

            MappingNode mappingNode = (MappingNode)node;
            mappingManagerClient.removeMapping(mappingNode.getMappingName());
        }

        partitionClient.store();

        ServersView serversView = ServersView.getInstance();
        serversView.refresh(parent);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void copy() throws Exception {

        log.debug("Copying mappings:");

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        MappingManagerClient mappingManagerClient = partitionClient.getMappingManagerClient();

        Collection<MappingConfig> list = new ArrayList<MappingConfig>();
        for (Node node : serversView.getSelectedNodes()) {
            if (!(node instanceof MappingNode)) continue;

            MappingNode mappingNode = (MappingNode)node;
            String mappingName = mappingNode.getMappingName();
            log.debug(" - "+mappingName);

            MappingClient mappingClient = mappingManagerClient.getMappingClient(mappingName);
            MappingConfig mappingConfig = mappingClient.getMappingConfig();
            list.add(mappingConfig);
        }

        serversView.getSWTClipboard().setContents(
                new Object[] { list.toArray(new MappingConfig[list.size()]) },
                new Transfer[] { MappingTransfer.getInstance() }
        );
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
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

    public MappingsNode getMappingsNode() {
        return mappingsNode;
    }

    public void setMappingsNode(MappingsNode mappingsNode) {
        this.mappingsNode = mappingsNode;
    }

    public boolean hasChildren() {
        return false;
    }
}