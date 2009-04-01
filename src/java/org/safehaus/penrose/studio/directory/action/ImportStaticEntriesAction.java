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
import org.safehaus.penrose.studio.directory.tree.DirectoryNode;
import org.safehaus.penrose.studio.directory.tree.EntryNode;
import org.safehaus.penrose.studio.directory.wizard.ImportEntriesWizard;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.ldap.DN;
import org.apache.log4j.Logger;

public class ImportStaticEntriesAction extends Action {

    Logger log = Logger.getLogger(getClass());

    Server server;
    String partitionName;
    DN targetDn;

    Node node;

	public ImportStaticEntriesAction(DirectoryNode node) {
        this((Node)node);
        server = node.getServerNode().getServer();
        partitionName = node.getPartitionName();
        targetDn = new DN();
    }

    public ImportStaticEntriesAction(EntryNode node) {
        this((Node)node);
        server = node.getServerNode().getServer();
        partitionName = node.getPartitionName();
        targetDn = node.getDn();
    }

    public ImportStaticEntriesAction(Node node) {
        this.node = node;

        setText("Import Static Entries...");
        setId(getClass().getName());
	}
	
	public void run() {
        try {
            ServersView serversView = ServersView.getInstance();

            ImportEntriesWizard wizard = new ImportEntriesWizard();
            wizard.setServer(server);
            wizard.setPartitionName(partitionName);
            wizard.setTargetDn(targetDn);

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