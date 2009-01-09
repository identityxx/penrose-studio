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
package org.safehaus.penrose.studio.mapping.action;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.mapping.MappingConfig;
import org.safehaus.penrose.mapping.MappingManagerClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.mapping.MappingsNode;
import org.safehaus.penrose.studio.mapping.wizard.AddMappingWizard;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.ServersView;

public class NewMappingAction extends Action {

    Logger log = Logger.getLogger(getClass());

    MappingsNode node;

	public NewMappingAction(MappingsNode node) {
        this.node = node;

        setText("New Mapping...");
        setId(getClass().getName());
	}

	public void run() {
        try {
            ServersView serversView = ServersView.getInstance();
            Server project = node.getProjectNode().getServer();

            AddMappingWizard wizard = new AddMappingWizard();

            WizardDialog dialog = new WizardDialog(serversView.getSite().getShell(), wizard);
            dialog.setPageSize(600, 300);
            int rc = dialog.open();

            if (rc == WizardDialog.CANCEL) return;

            MappingConfig mappingConfig = wizard.getMappingConfig();
            PartitionClient partitionClient = project.getClient().getPartitionManagerClient().getPartitionClient(node.getPartitionName());
            MappingManagerClient mappingManagerClient = partitionClient.getMappingManagerClient();

            mappingManagerClient.createMapping(mappingConfig);

            partitionClient.store();

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

            serversView.open(node);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
	}

}