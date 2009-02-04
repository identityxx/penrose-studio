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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.*;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.safehaus.penrose.studio.util.ChangeListener;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.util.Helper;
import org.safehaus.penrose.studio.tree.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ServersView extends ViewPart implements ChangeListener, ISelectionChangedListener {

    Logger log = Logger.getLogger(getClass());

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

    ServersContentProvider contentProvider;
    TreeViewer treeViewer;

    Object clipboard;

    public ServersView() {
    }
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		try {
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
                        //IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
                        Object object = selection.getFirstElement();

                        Node node = (Node)object;
                        node.open();

                        //treeViewer.refresh();

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
                        node.expand();

                        treeViewer.setExpandedState(object, true);

                        PenroseStudio penroseStudio = PenroseStudio.getInstance();
                        penroseStudio.notifyChangeListeners();

                        //treeViewer.refresh();

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

                        //treeViewer.refresh();

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        ErrorDialog.open(e);
                    }
                }
            });

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
			penroseStudio.addChangeListener(this);

		} catch (Exception ex) {
			log.debug(ex.toString(), ex);
		}
	}

    public void setClipboard(Object object) throws Exception {
        this.clipboard = object;
    }

    public Object getClipboard() {
        return clipboard;
    }

	public void setFocus() {
		treeViewer.getControl().setFocus();
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
	}
	
    public void handleChange(Object o) {
        treeViewer.refresh();
	}

    public boolean isExpanded(Object object) {
        return treeViewer.getExpandedState(object);
    }
    
    public void open(Object object) {
        treeViewer.setExpandedState(object, true);
        treeViewer.refresh();
    }

    public void close(Object object) {
        treeViewer.setExpandedState(object, false);
        treeViewer.refresh();
    }

    public TreeViewer getTreeViewer() {
        return treeViewer;
    }

    public void setTreeViewer(TreeViewer treeViewer) {
        this.treeViewer = treeViewer;
    }

    public void addProjectConfig(ServerConfig projectConfig) {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.getApplicationConfig().addProject(projectConfig);
        penroseStudio.saveApplicationConfig();

        contentProvider.addProjectConfig(projectConfig);
    }

    public void removeProjectConfig(String name) {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.getApplicationConfig().removeProject(name);
        penroseStudio.saveApplicationConfig();

        contentProvider.removeProjectConfig(name);
    }

    public ServerNode getSelectedServerNode() {
        IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
        if (selection == null) return null;

        Node node = (Node)selection.getFirstElement();

        while (node != null) {
            if (node instanceof ServerNode) return (ServerNode)node;
            node = node.getParent();
        }

        return null;
    }

    public Collection<Node> getSelectedNodes() {
        Collection<Node> list = new ArrayList<Node>();
        IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
        for (Iterator i=selection.iterator(); i.hasNext(); ) {
            Node node = (Node)i.next();
            list.add(node);
        }
        return list;
    }

    public static ServersView getInstance() throws Exception {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        return (ServersView)page.showView(ServersView.class.getName());
    }
}