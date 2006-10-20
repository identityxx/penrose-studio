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
package org.safehaus.penrose.studio.source;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Image;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.adapter.PenroseStudioAdapter;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.partition.ConnectionConfig;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class SourceNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;
    Project project;

    private Partition partition;
    private ConnectionConfig connectionConfig;
    private SourceConfig sourceConfig;

    public SourceNode(
            ObjectsView view,
            Project project,
            String name,
            String type,
            Image image,
            Object object,
            Node parent
    ) {
        super(name, type, image, object, parent);
        this.view = view;
        this.project = project;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Copy") {
            public void run() {
                try {
                    copy();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        ConnectionConfig con = partition.getConnectionConfig(sourceConfig.getConnectionName());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        SourceEditorInput ei = new SourceEditorInput();
        ei.setProject(project);
        ei.setPartition(partition);
        ei.setSourceConfig(sourceConfig);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioAdapter adapter = penroseStudio.getAdapter(con.getAdapterName());
        page.openEditor(ei, adapter.getSourceEditorClassName());

        /*
        if ("JDBC".equals(con.getAdapterName())) {
            page.openEditor(ei, JDBCSourceEditor.class.getName());

        } else if ("LDAP".equals(con.getAdapterName())) {
            page.openEditor(ei, LDAPSourceEditor.class.getName());
        }
        */
    }

    public void remove() throws Exception {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        TreeViewer treeViewer = view.getTreeViewer();
        IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();

        boolean confirm = MessageDialog.openQuestion(shell,
                "Confirmation", "Remove selected sources?");

        if (!confirm) return;

        for (Iterator i=selection.iterator(); i.hasNext(); ) {
            Node node = (Node)i.next();
            if (!(node instanceof SourceNode)) continue;

            SourceNode sourceNode = (SourceNode)node;
            SourceConfig sourceConfig = sourceNode.getSourceConfig();
            partition.removeSourceConfig(sourceConfig.getName());
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();
    }

    public void copy() throws Exception {
        view.setClipboard(getObject());
    }

    public void paste() throws Exception {

        Object newObject = view.getClipboard();

        if (!(newObject instanceof SourceConfig)) return;

        SourceConfig newSourceDefinition = (SourceConfig)((SourceConfig)newObject).clone();

        int counter = 1;
        String name = newSourceDefinition.getName();
        while (partition.getSourceConfig(name) != null) {
            counter++;
            name = newSourceDefinition.getName()+" ("+counter+")";
        }

        newSourceDefinition.setName(name);
        partition.addSourceConfig(newSourceDefinition);

        view.setClipboard(null);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }
}
