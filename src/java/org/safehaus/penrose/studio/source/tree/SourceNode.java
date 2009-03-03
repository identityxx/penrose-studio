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
package org.safehaus.penrose.studio.source.tree;

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
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.partition.tree.PartitionNode;
import org.safehaus.penrose.studio.partition.tree.PartitionsNode;
import org.safehaus.penrose.studio.plugin.Plugin;
import org.safehaus.penrose.studio.plugin.PluginManager;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.source.editor.SourceEditorInput;
import org.safehaus.penrose.studio.source.dnd.SourceTransfer;
import org.safehaus.penrose.studio.tree.Node;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class SourceNode extends Node {

    Logger log = Logger.getLogger(getClass());

    protected ServersView serversView;
    protected ServerNode serverNode;
    protected PartitionsNode partitionsNode;
    protected PartitionNode partitionNode;
    protected SourcesNode sourcesNode;

    private String partitionName;
    private String adapterName;
    private String sourceName;

    public SourceNode(String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
        sourcesNode = (SourcesNode)parent;
        partitionNode = sourcesNode.getPartitionNode();
        partitionsNode = partitionNode.getPartitionsNode();
        serverNode = partitionsNode.getServerNode();
        serversView = serverNode.getServersView();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
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
                    throw new RuntimeException(e);
                }
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    sourcesNode.paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        });

        manager.add(new Action("Delete", PenroseStudio.getImageDescriptor(PenroseImage.DELETE_SMALL)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void open() throws Exception {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PluginManager pluginManager = penroseStudio.getPluginManager();
        Plugin plugin = pluginManager.getPlugin(adapterName);

        SourceEditorInput ei = plugin.createSourceEditorInput();
        ei.setServer(serverNode.getServer());
        ei.setPartitionName(partitionName);
        ei.setSourceName(sourceName);

        String sourceEditorClassName = plugin.getSourceEditorClass();

        log.debug("Opening "+sourceEditorClassName);
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, sourceEditorClassName);
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                serversView.getSite().getShell(),
                "Confirmation", "Remove selected sources?");

        if (!confirm) return;

        Server project = serverNode.getServer();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

        for (Node node : serversView.getSelectedNodes()) {
            if (!(node instanceof SourceNode)) continue;

            SourceNode sourceNode = (SourceNode)node;
            sourceManagerClient.removeSource(sourceNode.getSourceName());
        }

        partitionClient.store();

        ServersView serversView = ServersView.getInstance();
        serversView.refresh(parent);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void copy() throws Exception {

        log.debug("Copying sources:");

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

        Collection<SourceConfig> list = new ArrayList<SourceConfig>();
        for (Node node : serversView.getSelectedNodes()) {
            if (!(node instanceof SourceNode)) continue;

            SourceNode sourceNode = (SourceNode)node;
            String sourceName = sourceNode.getSourceName();
            log.debug(" - "+sourceName);

            SourceClient sourceClient = sourceManagerClient.getSourceClient(sourceName);
            SourceConfig sourceConfig = sourceClient.getSourceConfig();
            list.add(sourceConfig);
        }

        serversView.getSWTClipboard().setContents(
                new Object[] { list.toArray(new SourceConfig[list.size()]) },
                new Transfer[] { SourceTransfer.getInstance() }
        );
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public boolean hasChildren() {
        return false;
    }
}
