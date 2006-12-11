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
package org.safehaus.penrose.studio.object;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.safehaus.penrose.studio.event.ChangeListener;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.server.ServersNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.tree.Node;

import java.util.*;

public class ObjectsView extends ViewPart implements ChangeListener, ISelectionChangedListener {

    Logger log = Logger.getLogger(getClass());

    public final static String SERVERS           = "Servers";
    public final static String LOCAL_FILES       = "Local Files";
    public final static String LIBRARIES         = "Libraries";

    public final static String PROJECT           = "Project";

    public final static String PARTITIONS        = "Partitions";
    public final static String PARTITION         = "Partition";

    public final static String DIRECTORY         = "Directory";
    public final static String ENTRY             = "Entry";

    public final static String CONNECTIONS       = "Connections";
    public final static String CONNECTION        = "Connection";

    public final static String SOURCES           = "Sources";
    public final static String SOURCE            = "Source";

    public final static String MODULES           = "Modules";
    public final static String MODULE            = "Module";

    public final static String CACHES            = "Caches";
    public final static String ENTRY_CACHE       = "Entry Cache";
    public final static String SOURCE_CACHE      = "Source Cache";
    public final static String ENGINES           = "Engines";
    public final static String ENGINE            = "Engine";
    public final static String ENGINE_CACHE      = "Engine Cache";
    public final static String CONNECTORS        = "Connectors";
    public final static String CONNECTOR         = "Connector";
    public final static String CONNECTOR_CACHE   = "Connector Cache";

    public final static String SCHEMAS           = "Schemas";
    public final static String SCHEMA            = "Schema";
    public final static String OBJECT_CLASSES    = "Object Classes";
    public final static String OBJECT_CLASS      = "Object Class";
    public final static String ATTRIBUTE_TYPES   = "Attribute Types";
    public final static String ATTRIBUTE_TYPE    = "Attribute Type";
    public final static String SERVICES          = "Services";
    public final static String SERVICE           = "Service";
    public final static String ADMINISTRATOR     = "Administrator";
    public final static String ROOT_DSE          = "Root DSE";
    public final static String SYSTEM_PROPERTIES = "System Properties";
    public final static String LOGGING           = "Logging";
    public final static String APPENDERS         = "Appenders";
    public final static String APPENDER          = "Appender";
    public final static String LOGGERS           = "Loggers";
    public final static String LOGGER            = "Logger";

    TreeViewer treeViewer;

    Object clipboard;

    Collection nodes = new ArrayList();
    ServersNode serversNode;

    public ObjectsView() {

        serversNode = new ServersNode(
                SERVERS,
                SERVERS,
                null
        );

        nodes.add(serversNode);
/*
        Node localPartitions = new Node(
                LOCAL_FILES,
                LOCAL_FILES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                LOCAL_FILES,
                null
        );

        nodes.add(localPartitions);

        Node library = new Node(
                LIBRARIES,
                LIBRARIES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                LIBRARIES,
                null
        );

        nodes.add(library);
*/
    }

    public Collection getServerNodes() throws Exception {
        return serversNode.getChildren();
    }

    public ServerNode getServerNode(String name) throws Exception {
        Collection serverNodes = serversNode.getChildren();
        for (Iterator i=serverNodes.iterator(); i.hasNext(); ) {
            ServerNode serverNode = (ServerNode)i.next();
            Server server = serverNode.getServer();
            if (name.equals(server.getName())) return serverNode;
        }
        return null;
    }

    public void createPartControl(final Composite parent) {
        try {
            treeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

            treeViewer.setContentProvider(new ObjectsContentProvider(this));
            treeViewer.setLabelProvider(new ObjectsLabelProvider(this));
            treeViewer.setInput(getViewSite());
            treeViewer.addSelectionChangedListener(this);

            MenuManager menuManager = new MenuManager("#PopupMenu");
            menuManager.setRemoveAllWhenShown(true);

            menuManager.addMenuListener(new IMenuListener() {
                public void menuAboutToShow(IMenuManager manager) {

                    try {
                        if (treeViewer.getTree().getSelectionCount() == 0) return;
                        TreeItem item = treeViewer.getTree().getSelection()[0];
                        Object object = item.getData();

                        Node node = (Node)object;
                        node.showMenu(manager);

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);

                        MessageDialog.openError(
                                parent.getShell(),
                                "ERROR",
                                e.getMessage()
                        );
                    }
                }
            });

            Menu menu = menuManager.createContextMenu(treeViewer.getControl());
            treeViewer.getControl().setMenu(menu);

            treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event) {

                    try {
                        StructuredSelection selection = (StructuredSelection)event.getSelection();
                        if (selection.isEmpty()) return;

                        Object object = selection.getFirstElement();
                        SelectionEvent e = new SelectionEvent(new Date(), object);

                        PenroseStudio penroseStudio = PenroseStudio.getInstance();
                        penroseStudio.fireSelectionEvent(e);

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);

                        MessageDialog.openError(
                                parent.getShell(),
                                "ERROR",
                                e.getMessage()
                        );
                    }
                }
            });

            treeViewer.addDoubleClickListener(new IDoubleClickListener() {
                public void doubleClick(DoubleClickEvent event) {
                    try {
                        ISelection selection = treeViewer.getSelection();
                        Object object = ((IStructuredSelection)selection).getFirstElement();

                        Node node = (Node)object;
                        node.open();

                        treeViewer.refresh();

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);

                        MessageDialog.openError(
                                parent.getShell(),
                                "ERROR",
                                e.getMessage()
                        );
                    }
                }
            });

            treeViewer.addTreeListener(new ITreeViewerListener() {
                public void treeCollapsed(TreeExpansionEvent event) {
                    try {
                        /*
                        ISelection selection = treeViewer.getSelection();
                        Object object = ((IStructuredSelection)selection).getFirstElement();

                        Node node = (Node)object;
                        node.collapse();

                        treeViewer.refresh();
                        */

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);

                        MessageDialog.openError(
                                parent.getShell(),
                                "ERROR",
                                e.getMessage()
                        );
                    }
                }

                public void treeExpanded(TreeExpansionEvent event) {
                    try {
                        Node node = (Node)event.getElement();
                        node.expand();

                        //treeViewer.refresh();

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);

                        MessageDialog.openError(
                                parent.getShell(),
                                "ERROR",
                                e.getMessage()
                        );
                    }
                }
            });

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.addChangeListener(this);

            show(serversNode);

        } catch (Exception ex) {
            log.debug(ex.toString(), ex);
        }
    }

    public void setClipboard(Object object) throws Exception {
        this.clipboard = object;
    }

    public Object getClipboard() throws Exception {
        return clipboard;
    }

    public void setFocus() {
        treeViewer.getControl().setFocus();
    }

    public void selectionChanged(SelectionChangedEvent event) {
    }

    public void objectChanged(ChangeEvent event) {
        treeViewer.refresh();
    }

    public void show(Object object) {
        treeViewer.setExpandedState(object, true);
    }

    public TreeViewer getTreeViewer() {
        return treeViewer;
    }

    public void setTreeViewer(TreeViewer treeViewer) {
        this.treeViewer = treeViewer;
    }

    public Node getSelectedNode() {
        StructuredSelection selection = (StructuredSelection)treeViewer.getSelection();
        if (selection.isEmpty()) return null;

        return (Node)selection.getFirstElement();
    }

    public Collection getSelectedNodes() {
        StructuredSelection selection = (StructuredSelection)treeViewer.getSelection();
        if (selection.isEmpty()) return null;

        return selection.toList();
    }

    public ServerNode getSelectedServerNode() {
        Node node = getSelectedNode();

        while (node != null && !(node instanceof ServerNode)) node = node.getParent();

        if (node == null) return null;
        return (ServerNode)node;
    }

    public Collection getNodes() {
        return nodes;
    }
}