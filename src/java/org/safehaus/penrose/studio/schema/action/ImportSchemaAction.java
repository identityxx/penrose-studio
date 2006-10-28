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
package org.safehaus.penrose.studio.schema.action;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.schema.wizard.ImportSchemaWizard;
import org.apache.log4j.Logger;

public class ImportSchemaAction extends Action {

    Logger log = Logger.getLogger(getClass());

	public ImportSchemaAction() {
        setText("Import Schema...");
        setId(getClass().getName());
	}
	
	public void run() {
        try {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            Shell shell = window.getShell();

            ImportSchemaWizard wizard = new ImportSchemaWizard();
            WizardDialog dialog = new WizardDialog(shell, wizard);
            dialog.setPageSize(600, 300);
            dialog.open();

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            Node node = penroseStudio.getServerNode("Penrose Server");
            penroseStudio.show(node);

            penroseStudio.fireChangeEvent();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
	}
	
}