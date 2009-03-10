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
package org.safehaus.penrose.studio.module.tree;

import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.partition.tree.PartitionsNode;
import org.safehaus.penrose.studio.partition.tree.PartitionNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.module.editor.ModuleEditorInput;
import org.safehaus.penrose.studio.module.editor.ModuleEditor;
import org.safehaus.penrose.studio.module.dnd.ModuleTransfer;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.module.ModuleClient;
import org.safehaus.penrose.module.ModuleManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.dnd.Transfer;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class ModuleNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ServersView serversView;
    private ServerNode serverNode;
    private PartitionsNode partitionsNode;
    private PartitionNode partitionNode;
    private ModulesNode modulesNode;

    private String partitionName;
    private String moduleName;

    public ModuleNode(String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);

        modulesNode = (ModulesNode)parent;
        partitionNode = modulesNode.getPartitionNode();
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
                    ErrorDialog.open(e);
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
                    ErrorDialog.open(e);
                }
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    modulesNode.paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        manager.add(new Action("Delete", PenroseStudio.getImageDescriptor(PenroseImage.DELETE_SMALL)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });
	}

    public void open() throws Exception {

        ModuleEditorInput ei = new ModuleEditorInput();
        ei.setServer(serverNode.getServer());
        ei.setPartitionName(partitionName);
        ei.setModuleName(moduleName);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, ModuleEditor.class.getName());
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                serversView.getSite().getShell(),
                "Confirmation", "Remove selected modules?"
        );

        if (!confirm) return;

        Server server = serverNode.getServer();

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        ModuleManagerClient moduleManagerClient = partitionClient.getModuleManagerClient();

        for (Node node : serversView.getSelectedNodes()) {
            if (!(node instanceof ModuleNode)) continue;

            ModuleNode moduleNode = (ModuleNode)node;
            moduleManagerClient.removeModule(moduleNode.getModuleName());
        }

        partitionClient.store();

        ServersView serversView = ServersView.getInstance();
        serversView.refresh(parent);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void copy() throws Exception {

        log.debug("Copying module:");

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        ModuleManagerClient moduleManagerClient = partitionClient.getModuleManagerClient();

        Collection<ModuleConfig> list = new ArrayList<ModuleConfig>();
        for (Node node : serversView.getSelectedNodes()) {
            if (!(node instanceof ModuleNode)) continue;

            ModuleNode moduleNode = (ModuleNode)node;
            String moduleName = moduleNode.getModuleName();
            log.debug(" - "+moduleName);

            ModuleClient moduleClient = moduleManagerClient.getModuleClient(moduleName);
            ModuleConfig moduleConfig = moduleClient.getModuleConfig();
            list.add(moduleConfig);
        }

        serversView.getSWTClipboard().setContents(
                new Object[] { list.toArray(new ModuleConfig[list.size()]) },
                new Transfer[] { ModuleTransfer.getInstance() }
        );
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
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

    public ModulesNode getModulesNode() {
        return modulesNode;
    }

    public void setModulesNode(ModulesNode modulesNode) {
        this.modulesNode = modulesNode;
    }

    public boolean hasChildren() {
        return false;
    }
}
