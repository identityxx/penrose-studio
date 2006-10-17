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
import org.eclipse.swt.SWT;
import org.safehaus.penrose.studio.event.ChangeListener;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.tree.Node;

import java.util.*;

public class ObjectsView extends ViewPart implements ChangeListener, ISelectionChangedListener {

    Logger log = Logger.getLogger(getClass());

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

    private TreeViewer treeViewer;

    Object clipboard;

    Map projects = new TreeMap();

    public ObjectsView() {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.loadApplicationConfig();

        Collection list = penroseStudio.getApplicationConfig().getProjects();
        for (Iterator i=list.iterator(); i.hasNext(); ) {
            Project project = (Project)i.next();
            createProjectNode(project);
        }
    }

    public void createProjectNode(Project project) {
        ProjectNode projectNode = new ProjectNode(
                this,
                project.getName(),
                PROJECT,
                project,
                null
        );

        projects.put(project.getName(), projectNode);
    }

    public void removeProjectNode(String projectName) {
        projects.remove(projectName);
    }

    public Collection getProjectNodes() {
        return projects.values();
    }

    public ProjectNode getProjectNode(String name) {
        return (ProjectNode)projects.get(name);
    }

    public void createPartControl(Composite parent) {
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
                        log.debug(e.getMessage(), e);
                    }
                }
            });

            Menu menu = menuManager.createContextMenu(treeViewer.getControl());
            treeViewer.getControl().setMenu(menu);

            treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event) {

                    StructuredSelection selection = (StructuredSelection)event.getSelection();
                    if (selection.isEmpty()) return;

                    Object object = selection.getFirstElement();
                    SelectionEvent e = new SelectionEvent(new Date(), object);

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.fireSelectionEvent(e);
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
                        log.debug(e.getMessage(), e);
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

    public ProjectNode getSelectedProjectNode() {
        Node node = getSelectedNode();

        while (node != null && !(node instanceof ProjectNode)) node = node.getParent();

        if (node == null) return null;
        return (ProjectNode)node;
    }
}