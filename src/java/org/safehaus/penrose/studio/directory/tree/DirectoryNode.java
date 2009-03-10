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
package org.safehaus.penrose.studio.directory.tree;

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
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.action.RefreshAction;
import org.safehaus.penrose.studio.schema.action.NewSchemaEntryAction;
import org.safehaus.penrose.studio.schema.action.NewADSchemaProxyAction;
import org.safehaus.penrose.studio.rootDse.action.NewRootDSEAction;
import org.safehaus.penrose.studio.rootDse.action.NewRootDSEProxyAction;
import org.safehaus.penrose.studio.directory.action.*;
import org.safehaus.penrose.studio.directory.dnd.EntryTransfer;
import org.safehaus.penrose.studio.partition.tree.PartitionNode;
import org.safehaus.penrose.studio.partition.tree.PartitionsNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.ldap.DN;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class DirectoryNode extends Node {

    Logger log = Logger.getLogger(getClass());

    protected ServersView serversView;
    protected ServerNode serverNode;

    private String partitionName;

    public DirectoryNode(String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
        PartitionNode partitionNode = (PartitionNode)parent;
        PartitionsNode partitionsNode = partitionNode.getPartitionsNode();
        serverNode = partitionsNode.getServerNode();
        serversView = serverNode.getServersView();
    }

    public void update() throws Exception {
        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        DirectoryClient directoryClient = partitionClient.getDirectoryClient();

        for (String entryName : directoryClient.getRootEntryNames()) {
            EntryClient entryClient = directoryClient.getEntryClient(entryName);
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
            entryNode.setEntryName(entryName);
            entryNode.setEntryConfig(entryConfig);
            entryNode.init();
            
            addChild(entryNode);
        }
    }

    public void expand() throws Exception {
        if (children == null) update();
    }

    public void refresh() throws Exception {
        removeChildren();
        update();
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new NewRootEntryAction(this));
        manager.add(new NewRootProxyAction(this));
        manager.add(new ImportStaticEntriesAction(this));

        if (PartitionConfig.ROOT.equals(partitionName)) {

            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

            manager.add(new NewRootDSEAction(this));
            manager.add(new NewRootDSEProxyAction(this));

            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

            manager.add(new NewSchemaEntryAction(this));
            manager.add(new NewADSchemaProxyAction(this));
        }

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
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new RefreshAction(this));
    }

    public void paste() throws Exception {

        log.debug("Pasting entries:");

        EntryConfig[] entryConfigs = (EntryConfig[]) serversView.getSWTClipboard().getContents(EntryTransfer.getInstance());
        if (entryConfigs == null) return;

        Server server = serverNode.getServer();

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        DirectoryClient directoryClient = partitionClient.getDirectoryClient();

        Collection<String> names = directoryClient.getEntryNames();

        for (EntryConfig entryConfig : entryConfigs) {
            String name = entryConfig.getName();

            int counter = 1;
            String newName = name;
            while (names.contains(newName)) {
                counter++;
                newName = name+"_"+counter;
            }

            log.debug(" - "+name+" -> "+newName);
            entryConfig.setName(newName);

            DN dn = new DN(entryConfig.getRdn());
            entryConfig.setDn(dn);

            directoryClient.createEntry(entryConfig);
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
}
