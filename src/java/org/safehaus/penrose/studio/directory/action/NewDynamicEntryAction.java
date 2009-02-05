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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.directory.wizard.DynamicEntryWizard;
import org.safehaus.penrose.studio.directory.node.EntryNode;
import org.safehaus.penrose.studio.server.node.ServerNode;
import org.safehaus.penrose.directory.EntryConfig;
import org.apache.log4j.Logger;

public class NewDynamicEntryAction extends Action {

    Logger log = Logger.getLogger(getClass());

    EntryNode node;

	public NewDynamicEntryAction(EntryNode node) {
        this.node = node;

        setText("New Dynamic Entry...");
        setId(getClass().getName());
	}
	
	public void run() {
        try {
            EntryConfig entryConfig = new EntryConfig();

            ServersView serversView = ServersView.getInstance();
            ServerNode projectNode = node.getServerNode();

            DynamicEntryWizard wizard = new DynamicEntryWizard();
            wizard.setEntryConfig(entryConfig);
            wizard.setServer(projectNode.getServer());
            wizard.setPartitionName(node.getPartitionName());
            wizard.setParentDn(node.getEntryConfig().getDn());

            WizardDialog dialog = new WizardDialog(serversView.getSite().getShell(), wizard);
            dialog.setPageSize(600, 300);
            int rc = dialog.open();

            if (rc == Window.CANCEL) return;

            node.refresh();

            //PenroseStudio penroseStudio = PenroseStudio.getInstance();
            //penroseStudio.notifyChangeListeners();

            serversView.open(node);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
	}
	
}