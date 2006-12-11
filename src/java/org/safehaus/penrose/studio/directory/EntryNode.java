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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.mapping.editor.MappingEditor;
import org.safehaus.penrose.studio.mapping.editor.MappingEditorInput;
import org.safehaus.penrose.studio.mapping.editor.MappingDialog;
import org.safehaus.penrose.studio.directory.action.NewStaticEntryAction;
import org.safehaus.penrose.studio.directory.action.NewDynamicEntryAction;
import org.safehaus.penrose.studio.directory.action.MapLDAPTreeAction;
import org.safehaus.penrose.studio.directory.action.NewEntryFromSourceAction;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.util.EntryUtil;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class EntryNode extends Node {

    Logger log = Logger.getLogger(getClass());

    Server server;

    private Partition partition;
    private EntryMapping entryMapping;

    public EntryNode(
            Server server,
            String name,
            Image image,
            Object object,
            Node parent
    ) {
        super(name, image, object, parent);
        this.server = server;
    }

    public void showMenu(IMenuManager manager) throws Exception {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(actions.getOpenAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Edit Sources") {
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

        manager.add(actions.getCopyAction());
        manager.add(actions.getPasteAction());
        manager.add(actions.getDeleteAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Properties") {
            public void run() {
                try {
                    editProperties();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
    }

    public void showCommercialMenu(IMenuManager manager) throws Exception {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseWorkbenchAdvisor workbenchAdvisor = penroseStudio.getWorkbenchAdvisor();
        PenroseWorkbenchWindowAdvisor workbenchWindowAdvisor = workbenchAdvisor.getWorkbenchWindowAdvisor();
        PenroseActionBarAdvisor actionBarAdvisor = workbenchWindowAdvisor.getActionBarAdvisor();

        //if (actionBarAdvisor.getShowCommercialFeaturesAction().isChecked()) {
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            manager.add(new NewEntryFromSourceAction(this));
            manager.add(new MapLDAPTreeAction(this));
        //}

    }

    public void open() throws Exception {

        MappingEditorInput mei = new MappingEditorInput();
        mei.setProject(server);
        mei.setPartition(partition);
        mei.setEntryDefinition(entryMapping);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        page.openEditor(mei, MappingEditor.class.getName());
    }

    public void editSources() throws Exception {

        MappingEditorInput mei = new MappingEditorInput();
        mei.setProject(server);
        mei.setPartition(partition);
        mei.setEntryDefinition(entryMapping);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        MappingEditor editor = (MappingEditor)page.openEditor(mei, MappingEditor.class.getName());
        editor.showSourcesPage();
    }

    public void editACL() throws Exception {

        MappingEditorInput mei = new MappingEditorInput();
        mei.setProject(server);
        mei.setPartition(partition);
        mei.setEntryDefinition(entryMapping);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        MappingEditor editor = (MappingEditor)page.openEditor(mei, MappingEditor.class.getName());
        editor.showACLPage();
    }

    public void editProperties() throws Exception {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        MappingDialog dialog = new MappingDialog(shell, SWT.NONE);
        dialog.setServer(server);
        dialog.setPartition(partition);
        dialog.setEntryMapping(entryMapping);
        dialog.open();
    }

    public Object copy() throws Exception {
        return null;
        //return entryMapping;
    }

    public boolean canPaste(Object object) throws Exception {
        return object instanceof EntryMapping;
    }

    public void paste(Object object) throws Exception {
        EntryMapping newEntryMapping = (EntryMapping)object;

        String rdn = newEntryMapping.getRdn();
        String dn = entryMapping.getDn();
        String newDn = EntryUtil.append(rdn, dn);

        newEntryMapping.setDn(newDn);

        partition.addEntryMapping(newEntryMapping);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();
    }

    public void delete() throws Exception {
        EntryMapping entryMapping = getEntryMapping();
        partition.removeEntryMapping(entryMapping);
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
                    server,
                    childMapping.getRdn(),
                    PenrosePlugin.getImage(PenroseImage.ENTRY),
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
