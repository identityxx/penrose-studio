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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.connection.tree.ConnectionsNode;
import org.safehaus.penrose.studio.directory.tree.DirectoryNode;
import org.safehaus.penrose.studio.mapping.tree.MappingsNode;
import org.safehaus.penrose.studio.module.tree.ModulesNode;
import org.safehaus.penrose.studio.partition.action.ExportPartitionAction;
import org.safehaus.penrose.studio.partition.editor.PartitionEditorInput;
import org.safehaus.penrose.studio.partition.editor.PartitionEditor;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.source.tree.SourcesNode;
import org.safehaus.penrose.studio.tree.Node;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class PartitionNode extends Node {

    public Logger log = Logger.getLogger(getClass());

    private ServersView view;
    private ServerNode serverNode;
    private PartitionsNode partitionsNode;

    private String partitionName;

    Action copyAction;

    public PartitionNode(String name, String partitionName, Node parent) throws Exception {
        super(name, PenroseStudio.getImage(PenroseImage.PARTITION), partitionName, parent);

        this.partitionName = partitionName;

        partitionsNode = (PartitionsNode)parent;
        serverNode = partitionsNode.getServerNode();
        view = serverNode.getServersView();
    }

    public void init() throws Exception {
        
        DirectoryNode directoryNode = new DirectoryNode(
                ServersView.DIRECTORY,
                PenroseStudio.getImage(PenroseImage.FOLDER),
                ServersView.DIRECTORY,
                this
        );

        directoryNode.setPartitionName(partitionName);
        directoryNode.init();

        addChild(directoryNode);

        MappingsNode mappingsNode = new MappingsNode(
                ServersView.MAPPINGS,
                PenroseStudio.getImage(PenroseImage.FOLDER),
                ServersView.MAPPINGS,
                this
        );

        mappingsNode.setPartitionName(partitionName);
        mappingsNode.init();

        addChild(mappingsNode);

        ConnectionsNode connectionsNode = new ConnectionsNode(
                ServersView.CONNECTIONS,
                PenroseStudio.getImage(PenroseImage.FOLDER),
                ServersView.CONNECTIONS,
                this
        );

        connectionsNode.setPartitionName(partitionName);
        connectionsNode.init();

        addChild(connectionsNode);

        SourcesNode sourcesNode = new SourcesNode(
                ServersView.SOURCES,
                PenroseStudio.getImage(PenroseImage.FOLDER),
                ServersView.SOURCES,
                this
        );

        sourcesNode.setPartitionName(partitionName);
        sourcesNode.init();

        addChild(sourcesNode);

        ModulesNode modulesNode = new ModulesNode(
                ServersView.MODULES,
                PenroseStudio.getImage(PenroseImage.FOLDER),
                ServersView.MODULES,
                this
        );

        modulesNode.setPartitionName(partitionName);
        modulesNode.init();

        addChild(modulesNode);
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

        manager.add(new Action("Start") {
            public void run() {
                try {
                    start();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Stop") {
            public void run() {
                try {
                    stop();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Restart") {
            public void run() {
                try {
                    restart();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        if (!PartitionConfig.ROOT.equals(partitionName)) {
            
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

            manager.add(new ExportPartitionAction(this));

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
    }

    public void open() throws Exception {

        PartitionEditorInput ei = new PartitionEditorInput();
        ei.setServer(serverNode.getServer());
        ei.setPartitionName(partitionName);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, PartitionEditor.class.getName());
    }

    public void start() throws Exception {
        log.debug("Starting "+name+" partition.");

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(name);
        partitionClient.start();
    }

    public void stop() throws Exception {
        log.debug("Stopping "+name+" partition.");

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(name);
        partitionClient.stop();
    }

    public void restart() throws Exception {
        log.debug("Restarting "+name+" partition.");

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(name);
        partitionClient.stop();
        partitionClient.start();
    }

    public void upload() throws Exception {
        log.debug("Uploading "+name+" partition.");

        Server project = serverNode.getServer();
        project.upload("partitions/"+name);
    }

    public void remove() throws Exception {

        Shell shell = view.getSite().getShell();

        boolean confirm = MessageDialog.openQuestion(
                shell,
                "Confirmation",
                "Remove Partition \""+ partitionName+"\"?");

        if (!confirm) return;

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof PartitionNode)) continue;

            PartitionNode partitionNode = (PartitionNode)node;
            partitionManagerClient.removePartition(partitionNode.getPartitionName());
        }

        ServersView serversView = ServersView.getInstance();
        serversView.refresh(parent);
        
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

    public PartitionsNode getPartitionsNode() {
        return partitionsNode;
    }

    public void setPartitionsNode(PartitionsNode partitionsNode) {
        this.partitionsNode = partitionsNode;
    }
}
