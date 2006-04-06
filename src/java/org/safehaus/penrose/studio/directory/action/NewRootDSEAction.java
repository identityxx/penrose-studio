/**
 * Copyright (c) 2000-2005, Identyx Corporation.
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
package org.safehaus.penrose.studio.directory.action;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.safehaus.penrose.studio.directory.DirectoryNode;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.mapping.MappingEditorInput;
import org.safehaus.penrose.studio.mapping.MappingEditor;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.partition.Partition;
import org.apache.log4j.Logger;

public class NewRootDSEAction extends Action {

    Logger log = Logger.getLogger(getClass());

    DirectoryNode node;

	public NewRootDSEAction(DirectoryNode node) {
        this.node = node;

        setText("New Root DSE...");
        setId(getClass().getName());
	}
	
	public void run() {
        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            EntryMapping entryMapping = new EntryMapping();

            Partition partition = node.getPartition();
            partition.addEntryMapping(entryMapping);

            PenroseApplication penroseApplication = PenroseApplication.getInstance();
            penroseApplication.notifyChangeListeners();

            objectsView.show(node);

            MappingEditorInput mei = new MappingEditorInput(partition, entryMapping);

            page.openEditor(mei, MappingEditor.class.getName());

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
	}
	
}