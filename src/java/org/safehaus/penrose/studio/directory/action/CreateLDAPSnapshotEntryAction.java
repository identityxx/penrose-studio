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
package org.safehaus.penrose.studio.directory.action;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.directory.DirectoryNode;
import org.safehaus.penrose.studio.directory.wizard.CreateLDAPSnapshotWizard;
import org.safehaus.penrose.studio.PenroseStudio;
import org.apache.log4j.Logger;

public class CreateLDAPSnapshotEntryAction extends Action {

    Logger log = Logger.getLogger(getClass());

    DirectoryNode node;

	public CreateLDAPSnapshotEntryAction(DirectoryNode node) {
        this.node = node;

        setText("Create LDAP Snapshot...");
        setId(getClass().getName());
	}
	
	public void run() {
        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            Shell shell = window.getShell();

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            //if (!penroseStudio.checkCommercial()) return;

            Wizard wizard = new CreateLDAPSnapshotWizard(node.getPartition());

            WizardDialog dialog = new WizardDialog(shell, wizard);
            dialog.setPageSize(600, 300);
            dialog.open();

            penroseStudio.fireChangeEvent();

            objectsView.show(node);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
	}
	
}