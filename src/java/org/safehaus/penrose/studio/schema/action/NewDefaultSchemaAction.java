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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.directory.node.DirectoryNode;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.schema.wizard.DefaultSchemaWizard;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.ServersView;
import org.apache.log4j.Logger;

public class NewDefaultSchemaAction extends Action {

    Logger log = Logger.getLogger(getClass());

    DirectoryNode directoryNode;

	public NewDefaultSchemaAction(DirectoryNode node) {
        this.directoryNode = node;

        setText("Create Default Schema...");
        setId(getClass().getName());
	}

	public void run() {
        try {
            ServersView serversView = ServersView.getInstance();
            Server server = directoryNode.getServerNode().getServer();
            PenroseStudio penroseStudio = PenroseStudio.getInstance();

            DefaultSchemaWizard wizard = new DefaultSchemaWizard();
            wizard.setServer(server);
            wizard.setPartitionName(directoryNode.getPartitionName());

            WizardDialog dialog = new WizardDialog(serversView.getSite().getShell(), wizard);
            dialog.setPageSize(600, 300);
            int rc = dialog.open();

            if (rc == Window.CANCEL) return;

            directoryNode.refresh();

            penroseStudio.notifyChangeListeners();

            serversView.open(directoryNode);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
	}

}