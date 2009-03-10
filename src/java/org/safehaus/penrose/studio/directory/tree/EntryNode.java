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
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntryClient;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.action.RefreshAction;
import org.safehaus.penrose.studio.directory.action.*;
import org.safehaus.penrose.studio.directory.editor.EntryEditorInput;
import org.safehaus.penrose.studio.directory.editor.EntryEditor;
import org.safehaus.penrose.studio.directory.dnd.EntryTransfer;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.ldap.DN;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Endi S. Dewata
 */
public class EntryNode extends Node {

    Logger log = Logger.getLogger(getClass());

    protected ServersView serversView;
    protected ServerNode serverNode;
    protected DirectoryNode directoryNode;

    private String partitionName;
    private String entryName;
    private DN dn;

    public EntryNode(String name, Image image, DN dn, Node parent) {
        super(name, image, null, parent);

        this.dn = dn;

        if (parent instanceof DirectoryNode) {
            directoryNode = (DirectoryNode)parent;
        } else if (parent instanceof EntryNode) {
            directoryNode = ((EntryNode)parent).getDirectoryNode();
        }

        serverNode = directoryNode.getServerNode();
        serversView = serverNode.getServersView();
    }

    public void update() throws Exception {

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        DirectoryClient directoryClient = partitionClient.getDirectoryClient();

        log.debug("Getting children:");

        for (String childName : directoryClient.getChildNames(entryName)) {
            log.debug(" - "+childName);

            DN childDn = directoryClient.getEntryDn(childName);
            String label = childDn.getRdn().toString();

            EntryNode entryNode = new EntryNode(
                    label,
                    PenroseStudio.getImage(PenroseImage.FOLDER),
                    childDn,
                    this
            );

            entryNode.setPartitionName(partitionName);
            entryNode.setEntryName(childName);
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

        manager.add(new NewStaticEntryAction(this));
        manager.add(new NewDynamicEntryAction(this));
        manager.add(new NewProxyEntryAction(this));
        manager.add(new ImportStaticEntriesAction(this));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Move Up") {
            public void run() {
                try {
                    moveUp();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        manager.add(new Action("Move Down") {
            public void run() {
                try {
                    moveDown();
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
                    paste();
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

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new RefreshAction(this));
    }

    public void open() throws Exception {

        EntryEditorInput ei = new EntryEditorInput();
        ei.setPartitionName(partitionName);
        ei.setEntryName(entryName);
        ei.setServer(serverNode.getServer());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        page.openEditor(ei, EntryEditor.class.getName());
    }

    public void moveUp() throws Exception {

        log.debug("Moving entries up:");

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        DirectoryClient directoryClient = partitionClient.getDirectoryClient();

        for (Node node : serversView.getSelectedNodes()) {
            if (!(node instanceof EntryNode)) continue;

            EntryNode entryNode = (EntryNode)node;
            String entryName = entryNode.getEntryName();
            String parentName = directoryClient.getParentName(entryName);

            List<String> childNames = directoryClient.getChildNames(parentName);
            int index = childNames.indexOf(entryName);
            if (index <= 0) continue;

            log.debug(" - "+entryName+": "+index+" -> "+(index-1));

            childNames.remove(index);
            childNames.add(index-1, entryName);
            directoryClient.setChildNames(parentName, childNames);
        }

        partitionClient.store();

        ServersView serversView = ServersView.getInstance();
        serversView.refresh(parent);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void moveDown() throws Exception {

        log.debug("Moving entries down:");

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        DirectoryClient directoryClient = partitionClient.getDirectoryClient();

        for (Node node : serversView.getSelectedNodes()) {
            if (!(node instanceof EntryNode)) continue;

            EntryNode entryNode = (EntryNode)node;
            String entryName = entryNode.getEntryName();
            String parentName = directoryClient.getParentName(entryName);

            List<String> childNames = directoryClient.getChildNames(parentName);
            int index = childNames.indexOf(entryName);
            if (index < 0 || index >= childNames.size() - 1) continue;

            log.debug(" - "+entryName+": "+index+" -> "+(index+1));

            childNames.remove(index);
            childNames.add(index+1, entryName);
            directoryClient.setChildNames(parentName, childNames);
        }

        partitionClient.store();

        ServersView serversView = ServersView.getInstance();
        serversView.refresh(parent);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void copy() throws Exception {

        log.debug("Copying entries:");

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        DirectoryClient directoryClient = partitionClient.getDirectoryClient();

        Collection<EntryConfig> list = new ArrayList<EntryConfig>();
        for (Node node : serversView.getSelectedNodes()) {
            if (!(node instanceof EntryNode)) continue;

            EntryNode entryNode = (EntryNode)node;
            String entryName = entryNode.getEntryName();
            log.debug(" - "+entryName);

            EntryClient entryClient = directoryClient.getEntryClient(entryName);
            EntryConfig entryConfig = entryClient.getEntryConfig();
            list.add(entryConfig);
        }

        serversView.getSWTClipboard().setContents(
                new Object[] { list.toArray(new EntryConfig[list.size()]) },
                new Transfer[] { EntryTransfer.getInstance() }
        );
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

            DN dn = entryConfig.getRdn().append(this.dn);
            entryConfig.setDn(dn);

            directoryClient.createEntry(entryConfig);
        }

        partitionClient.store();

        ServersView serversView = ServersView.getInstance();
        serversView.refresh(this);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                serversView.getSite().getShell(),
                "Confirmation", "Remove selected entries?"
        );

        if (!confirm) return;

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        DirectoryClient directoryClient = partitionClient.getDirectoryClient();

        for (Node node : serversView.getSelectedNodes()) {
            if (!(node instanceof EntryNode)) continue;

            EntryNode entryNode = (EntryNode) node;

            String entryName = entryNode.getEntryName();
            directoryClient.removeEntry(entryName);
        }

        partitionClient.store();

        ServersView serversView = ServersView.getInstance();
        serversView.refresh(parent);
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

    public DirectoryNode getDirectoryNode() {
        return directoryNode;
    }

    public void setDirectoryNode(DirectoryNode directoryNode) {
        this.directoryNode = directoryNode;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }


    public boolean hasChildren() throws Exception {

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        DirectoryClient directoryClient = partitionClient.getDirectoryClient();

        EntryClient entryClient = directoryClient.getEntryClient(entryName);

        return !entryClient.getChildNames().isEmpty();
    }

    public DN getDn() {
        return dn;
    }

    public void setDn(DN dn) {
        this.dn = dn;
    }
}
