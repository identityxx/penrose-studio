/**
 * Copyright 2009 Red Hat, Inc.
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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.directory.wizard.EntryWizard;
import org.safehaus.penrose.studio.directory.tree.EntryNode;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.directory.EntryConfig;
import org.apache.log4j.Logger;

public class NewEntryAction extends Action {

    Logger log = Logger.getLogger(getClass());

    EntryNode node;

	public NewEntryAction(EntryNode node) {
        this.node = node;

        setText("New Entry...");
        setId(getClass().getName());
	}
	
	public void run() {
        try {
            EntryConfig entryConfig = new EntryConfig();

            ServersView serversView = ServersView.getInstance();
            ServerNode serverNode = node.getServerNode();

            EntryWizard wizard = new EntryWizard();
            wizard.setServer(serverNode.getServer());
            wizard.setPartitionName(node.getPartitionName());
            wizard.setParentDn(node.getDn());
            wizard.setEntryConfig(entryConfig);

            WizardDialog dialog = new WizardDialog(serversView.getSite().getShell(), wizard);
            dialog.setPageSize(600, 300);
            int rc = dialog.open();

            if (rc == Window.CANCEL) return;

            serversView.refresh(node);
            serversView.open(node);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
	}
	
}