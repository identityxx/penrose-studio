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
package org.safehaus.penrose.studio.directory;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.mapping.*;
import org.safehaus.penrose.studio.directory.action.NewStaticEntryAction;
import org.safehaus.penrose.studio.directory.action.NewDynamicEntryAction;
import org.safehaus.penrose.studio.directory.action.MapLDAPTreeAction;
import org.safehaus.penrose.studio.directory.action.NewEntryFromSourceAction;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.partition.Partition;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class EntryNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    private Partition partition;
    private EntryMapping entryMapping;

    public EntryNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Edit sources") {
            public void run() {
                try {
                    editSources();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Edit ACL") {
            public void run() {
                try {
                    editACL();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new NewStaticEntryAction(this));
        manager.add(new NewDynamicEntryAction(this));

        showCommercialMenu(manager);

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Copy") {
            public void run() {
                try {
                    //copy(connection);
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    //paste(connection);
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

    public void showCommercialMenu(IMenuManager manager) throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseWorkbenchAdvisor workbenchAdvisor = penroseApplication.getWorkbenchAdvisor();
        PenroseWorkbenchWindowAdvisor workbenchWindowAdvisor = workbenchAdvisor.getWorkbenchWindowAdvisor();
        PenroseActionBarAdvisor actionBarAdvisor = workbenchWindowAdvisor.getActionBarAdvisor();

        if (actionBarAdvisor.getShowCommercialFeaturesAction().isChecked()) {
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            manager.add(new NewEntryFromSourceAction(this));
            manager.add(new MapLDAPTreeAction(this));
        }

    }

    public void open() throws Exception {

        MappingEditorInput mei = new MappingEditorInput(partition, entryMapping);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        page.openEditor(mei, MappingEditor.class.getName());
    }

    public void editSources() throws Exception {

        MappingEditorInput mei = new MappingEditorInput(partition, entryMapping);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        MappingEditor editor = (MappingEditor)page.openEditor(mei, MappingEditor.class.getName());
        editor.showSourcesPage();
    }

    public void editACL() throws Exception {

        MappingEditorInput mei = new MappingEditorInput(partition, entryMapping);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        MappingEditor editor = (MappingEditor)page.openEditor(mei, MappingEditor.class.getName());
        editor.showACLPage();
    }

    public void remove() throws Exception {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        TreeViewer treeViewer = view.getTreeViewer();
        IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();

        boolean confirm = MessageDialog.openQuestion(shell,
                "Confirmation", "Remove selected entries?");

        if (!confirm) return;

        for (Iterator i=selection.iterator(); i.hasNext(); ) {
            Node node = (Node)i.next();
            if (!(node instanceof EntryNode)) continue;

            EntryNode entryNode = (EntryNode)node;

            EntryMapping entryMapping = entryNode.getEntryMapping();
            partition.removeEntryMapping(entryMapping);
        }

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        penroseApplication.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        Collection children = partition.getChildren(entryMapping);
        return (children != null && children.size() > 0);
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        for (Iterator i=partition.getChildren(entryMapping).iterator(); i.hasNext(); ) {
            EntryMapping childMapping = (EntryMapping)i.next();

            EntryNode entryNode = new EntryNode(
                    view,
                    childMapping.getRdn(),
                    ObjectsView.ENTRY,
                    PenrosePlugin.getImage(PenroseImage.NODE),
                    childMapping,
                    this
            );

            entryNode.setPartition(partition);
            entryNode.setEntryMapping(childMapping);

            children.add(entryNode);
        }

        return children;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public EntryMapping getEntryMapping() {
        return entryMapping;
    }

    public void setEntryMapping(EntryMapping entryMapping) {
        this.entryMapping = entryMapping;
    }
}
