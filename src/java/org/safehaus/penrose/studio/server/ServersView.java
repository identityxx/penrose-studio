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
package org.safehaus.penrose.studio.server;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.dnd.*;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.tree.Node;

import java.util.ArrayList;
import java.util.Collection;

public class ServersView extends ViewPart implements ISelectionChangedListener {

    Logger log = Logger.getLogger(getClass());

    public final static int LINUX   = 0;
    public final static int WINDOWS = 1;

    public final static String PARTITION         = "Partition";
    public final static String DIRECTORY         = "Directory";
    public final static String ENTRY             = "Entry";
    public final static String DATA_SOURCES      = "Data Sources";
    public final static String CONNECTIONS       = "Connections";
    public final static String CONNECTION        = "Connection";
    public final static String MAPPINGS          = "Mappings";
    public final static String MAPPING           = "Mapping";
    public final static String SOURCES           = "Sources";
    public final static String SOURCE            = "Source";
    public final static String CACHES            = "Caches";
    public final static String ENTRY_CACHE       = "Entry Cache";
    public final static String SOURCE_CACHE      = "Source Cache";
    public final static String HANDLER           = "Handler";
    public final static String ENGINES           = "Engines";
    public final static String ENGINE            = "Engine";
    public final static String ENGINE_CACHE      = "Engine Cache";
    public final static String CONNECTORS        = "Connectors";
    public final static String CONNECTOR_CACHE   = "Connector Cache";
    public final static String MODULES           = "Modules";
    public final static String MODULE            = "Module";
    public final static String SCHEMA            = "Schema";
    public final static String BUILTIN_SCHEMA    = "Built-In Schema";
    public final static String CUSTOM_SCHEMA     = "Custom Schema";
    public final static String OBJECT_CLASSES    = "Object Classes";
    public final static String OBJECT_CLASS      = "Object Class";
    public final static String ATTRIBUTE_TYPES   = "Attribute Types";
    public final static String ATTRIBUTE_TYPE    = "Attribute Type";
    public final static String SERVICE           = "Service";
    public final static String PLUGIN            = "Plugin";
    public final static String ROOT_DSE          = "Root DSE";
    public final static String APPENDERS         = "Appenders";
    public final static String APPENDER          = "Appender";
    public final static String LOGGERS           = "Loggers";
    public final static String LOGGER            = "Logger";

    Tree tree;
    MenuManager menuManager;

    Collection<Node> serverNodes = new ArrayList<Node>();

    //ServersContentProvider contentProvider;
    //TreeViewer treeViewer;

    Clipboard swtClipboard;
    Object clipboard;

    int type;

    public ServersView() {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            type = LINUX;
        } else if (osName.startsWith("Windows")) {
            type = WINDOWS;
        }
    }
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		try {
            swtClipboard = new Clipboard(getSite().getShell().getDisplay());

            tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
            tree.setLayoutData(new GridData(GridData.FILL_BOTH));

            menuManager = new MenuManager("#PopupMenu");

            Menu menu = menuManager.createContextMenu(tree);
            tree.setMenu(menu);

            tree.addSelectionListener(new SelectionAdapter() {
                public void widgetDefaultSelected(SelectionEvent event) {
                    // disable expanding tree when double-clicked
                }
            });

            tree.addTreeListener(new TreeAdapter() {
                public void treeExpanded(TreeEvent event) {
                    try {
                        if (event.item == null) return;

                        TreeItem item = (TreeItem)event.item;
                        expand(item);

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        ErrorDialog.open(e);
                    }
                }
                public void treeCollapsed(TreeEvent event) {
                    try {
                        if (event.item == null) return;

                        TreeItem item = (TreeItem)event.item;
                        collapse(item);

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        ErrorDialog.open(e);
                    }
                }
            });

            tree.addMouseListener(new MouseAdapter() {
                public void mouseDoubleClick(MouseEvent event) {
                    try {
                        TreeItem item = tree.getItem(new Point(event.x, event.y));
                        if (item == null) return;

                        doubleClick(item);
                        
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        ErrorDialog.open(e);
                    }
                }
                public void mouseDown(MouseEvent event) {
                    try {
                        //log.debug("Button #"+event.button+" down at ("+event.x+","+event.y+").");
                        if (event.button != 3) return;

                        menuManager.removeAll();

                        if (type != LINUX) return;

                        TreeItem item = tree.getItem(new Point(event.x, event.y));
                        if (item == null) return;

                        rightClick(item);

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        ErrorDialog.open(e);
                    }
                }
                public void mouseUp(MouseEvent event) {
                    try {
                        //log.debug("Button #"+event.button+" up at ("+event.x+","+event.y+").");
                        if (event.button != 3) return;

                        menuManager.removeAll();

                        if (type != WINDOWS) return;

                        TreeItem item = tree.getItem(new Point(event.x, event.y));
                        if (item == null) return;

                        rightClick(item);

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        ErrorDialog.open(e);
                    }
                }
            });

            refresh();
/*
            contentProvider = new ServersContentProvider(this);

            treeViewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

			treeViewer.setContentProvider(contentProvider);
            treeViewer.setLabelProvider(new ServersLabelProvider(this));
			treeViewer.setInput(getViewSite());
			treeViewer.addSelectionChangedListener(this);

			Helper.hookContextMenu(treeViewer.getControl(), new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
                    try {
                        if (treeViewer.getTree().getSelectionCount() == 0) return;
                        TreeItem item = treeViewer.getTree().getSelection()[0];
                        Object object = item.getData();

                        Node node = (Node)object;
                        node.showMenu(manager);

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        ErrorDialog.open(e);
                    }
				}
			});

            treeViewer.addDoubleClickListener(new IDoubleClickListener() {
                public void doubleClick(DoubleClickEvent event) {
                    try {
                        IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                        Object object = selection.getFirstElement();

                        Node node = (Node)object;
                        node.open();

                        PenroseStudio penroseStudio = PenroseStudio.getInstance();
                        penroseStudio.notifyChangeListeners();

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        ErrorDialog.open(e);
                    }
                }
            });

            treeViewer.addTreeListener(new ITreeViewerListener() {
                public void treeExpanded(TreeExpansionEvent event) {
                    try {
                        Object object = event.getElement();
                        Node node = (Node)object;
                        log.debug("Expanding "+node.getName()+".");

                        node.expand();

                        boolean hasChildren = node.hasChildren();
                        log.debug("Has children: "+hasChildren);

                        Collection<Node> children = node.getChildren();
                        log.debug("Children: "+children);

                        if (children.isEmpty()) {
                            log.debug("Searching node "+node.getName()+":");
                            Tree tree = treeViewer.getTree();
                            TreeItem item = findTreeItem(tree.getItems(), node);
                            if (item != null) {
                                log.debug("Removing children of "+node.getName()+".");
                                for (TreeItem ti : item.getItems()) ti.dispose();
                                treeViewer.refresh();
                            }

                        } else {
                            log.debug("Expanding node "+node.getName()+".");
                            treeViewer.setExpandedState(object, true);
                        }

                        //PenroseStudio penroseStudio = PenroseStudio.getInstance();
                        //penroseStudio.notifyChangeListeners();

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        ErrorDialog.open(e);
                    }
                }
                public void treeCollapsed(TreeExpansionEvent event) {
                    try {
                        Object object = event.getElement();

                        Node node = (Node)object;
                        node.collapse();

                        treeViewer.setExpandedState(object, false);

                        PenroseStudio penroseStudio = PenroseStudio.getInstance();
                        penroseStudio.notifyChangeListeners();

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        ErrorDialog.open(e);
                    }
                }
            });

            treeViewer.addDragSupport(
                    DND.DROP_COPY | DND.DROP_MOVE,
                    new Transfer[] {
                            SourceTransfer.getInstance()
                    },
                    new DragSourceAdapter() {
                        public void dragStart(DragSourceEvent event) {
                            log.debug("Drag start.");
                        }
                        public void dragFinished(DragSourceEvent event) {
                            log.debug("Drag finished.");
                        }
                        public void dragSetData(DragSourceEvent event) {
                            log.debug("Drag set data:");
                            IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
                            Node[] nodes = (Node[])selection.toList().toArray(new Node[selection.size()]);
                            for (Node node : nodes) {
                                log.debug(" - "+node.getName());
                            }
                        }
                    }
            );

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
			penroseStudio.addChangeListener(this);
*/
		} catch (Exception ex) {
			log.debug(ex.toString(), ex);
		}
	}

    public void doubleClick(TreeItem item) throws Exception {

        Node node = (Node)item.getData();
        if (node == null) return;

        log.debug("Double-click node "+node.getName()+".");

        node.open();
    }

    public void rightClick(TreeItem item) throws Exception {

        Node node = (Node)item.getData();
        if (node == null) return;

        //log.debug("Right-click node "+node.getName()+".");

        node.showMenu(menuManager);

        menuManager.setVisible(true);
    }

    public void expand(TreeItem item) throws Exception {

        for (TreeItem ti : item.getItems()) ti.dispose();

        Node node = (Node)item.getData();
        //log.debug("Expanding "+node.getName()+".");

        node.expand();

        boolean hasChildren = node.hasChildren();
        //log.debug("Has children: "+hasChildren);

        if (!hasChildren) return;

        Collection<Node> children = node.getChildren();
        //log.debug("Children: "+children);

        if (children.isEmpty()) return;

        for (Node child : children) {
            TreeItem ti = new TreeItem(item, SWT.NONE);
            ti.setImage(child.getImage());
            ti.setText(child.getName());
            ti.setData(child);

            if (child.hasChildren()) new TreeItem(ti, SWT.NONE);
        }

        item.setExpanded(true);
    }

    public void collapse(TreeItem item) throws Exception {

        item.setExpanded(false);

        for (TreeItem ti : item.getItems()) ti.dispose();

        Node node = (Node)item.getData();
        //log.debug("Collapsing "+node.getName()+".");

        node.collapse();

        if (node.hasChildren()) new TreeItem(item, SWT.NONE);
    }

    public TreeItem findTreeItem(TreeItem[] items, Node node) {
        for (TreeItem item : items) {
            Node n = (Node)item.getData();
            if (n == null) continue;

            log.debug(" - "+n.getName());
            if (n == node) return item;

            TreeItem result = findTreeItem(item.getItems(), node);
            if (result != null) return result;
        }

        return null;
    }

    public void dispose() {
        swtClipboard.dispose();
    }
    
    public Clipboard getSWTClipboard() {
        return swtClipboard;
    }
    
    public void setClipboard(Object object) throws Exception {
        this.clipboard = object;
    }

    public Object getClipboard() {
        return clipboard;
    }

	public void setFocus() {
        tree.forceFocus();
        //treeViewer.getControl().setFocus();
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
	}
	
    public boolean isExpanded(Object object) {
        TreeItem item = findTreeItem(tree.getItems(), (Node)object);
        if (item == null) return false;
        return item.getExpanded();
        //return treeViewer.getExpandedState(object);
    }
    
    public void open(Node node) throws Exception {
        TreeItem item = findTreeItem(tree.getItems(), node);
        if (item == null) return;

        expand(item);

        //treeViewer.setExpandedState(object, true);
        //treeViewer.refresh();
    }

    public void close(Node node) throws Exception {
        TreeItem item = findTreeItem(tree.getItems(), node);
        if (item == null) return;

        collapse(item);

        //treeViewer.setExpandedState(object, false);
        //treeViewer.refresh();
    }

    public void addServerConfig(ServerConfig serverConfig) throws Exception {
        //contentProvider.addServerConfig(serverConfig);
    }

    public void removeServerConfig(String name) {

        //contentProvider.removeProjectConfig(name);
    }

    public void setSelection(Node node) {
        TreeItem item = findTreeItem(tree.getItems(), node);
        if (item == null) return;
        tree.setSelection(item);
        //treeViewer.setSelection(new StructuredSelection(node), true);
    }

    public ServerNode getSelectedServerNode() {
        if (tree.getSelectionCount() == 0) return null;

        TreeItem item = tree.getSelection()[0];
        Node node = (Node)item.getData();
/*
        IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
        if (selection == null) return null;

        Node node = (Node)selection.getFirstElement();
*/
        while (node != null) {
            if (node instanceof ServerNode) return (ServerNode)node;
            node = node.getParent();
        }

        return null;
    }

    public Collection<Node> getSelectedNodes() {
        Collection<Node> list = new ArrayList<Node>();
        if (tree.getSelectionCount() == 0) return list;

        for (TreeItem item : tree.getSelection()) {
            Node node = (Node)item.getData();
            list.add(node);
        }

/*
        IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
        for (Iterator i=selection.iterator(); i.hasNext(); ) {
            Node node = (Node)i.next();
            list.add(node);
        }
*/
        return list;
    }

    public static ServersView getInstance() throws Exception {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        return (ServersView)page.showView(ServersView.class.getName());
    }

    public void refresh() throws Exception {

        for (TreeItem item : tree.getItems()) item.dispose();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        for (ServerConfig serverConfig : penroseStudio.getApplicationConfig().getServerConfigs()) {

            ServerNode node = new ServerNode(
                    this,
                    serverConfig.getName(),
                    PenroseStudio.getImage(PenroseImage.LOGO),
                    serverConfig
            );

            TreeItem item = new TreeItem(tree, SWT.NONE);
            item.setImage(node.getImage());
            item.setText(node.getName());
            item.setData(node);

            new TreeItem(item, SWT.NONE);
        }
    }

    public void refresh(Node node) throws Exception {

        TreeItem item = findTreeItem(tree.getItems(), node);
        if (item == null) return;

        node.refresh();

        for (TreeItem ti : item.getItems()) ti.dispose();

        boolean hasChildren = node.hasChildren();
        //log.debug("Has children: "+hasChildren);

        if (!hasChildren) return;

        Collection<Node> children = node.getChildren();
        //log.debug("Children: "+children);

        if (children.isEmpty()) return;

        for (Node child : children) {
            TreeItem ti = new TreeItem(item, SWT.NONE);
            ti.setImage(child.getImage());
            ti.setText(child.getName());
            ti.setData(child);

            if (child.hasChildren()) new TreeItem(ti, SWT.NONE);
        }
    }
}
