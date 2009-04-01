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
package org.safehaus.penrose.studio.connection.action;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.connection.ConnectionClient;
import org.safehaus.penrose.connection.ConnectionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.source.wizard.SourceWizard;
import org.safehaus.penrose.studio.nis.source.wizard.NISSourceWizard;
import org.safehaus.penrose.studio.ldap.source.wizard.LDAPSourceWizard;
import org.safehaus.penrose.studio.connection.tree.ConnectionNode;
import org.safehaus.penrose.studio.jdbc.source.wizard.JDBCSourceWizard;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.source.SourceConfig;

public class NewSourceAction extends Action {

    Logger log = Logger.getLogger(getClass());

    ConnectionNode node;

	public NewSourceAction(ConnectionNode node) {
        this.node = node;

        setText("New Source...");
        setId(getClass().getName());
	}
	
	public void run() {
        try {
            ServersView serversView = ServersView.getInstance();
            Server server = node.getServerNode().getServer();

            String partitionName  = node.getPartitionName();
            String adapterName    = node.getAdapterName();
            String connectionName = node.getConnectionName();

            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            ConnectionManagerClient connectionManagerClient = partitionClient.getConnectionManagerClient();

            ConnectionClient connectionClient = connectionManagerClient.getConnectionClient(connectionName);
            ConnectionConfig connectionConfig = connectionClient.getConnectionConfig();

            SourceConfig sourceConfig = new SourceConfig();

            SourceWizard wizard;
            int rc = Window.CANCEL;

            if ("JDBC".equals(adapterName)) {
                wizard = new JDBCSourceWizard();
                wizard.setServer(server);
                wizard.setPartitionName(partitionName);
                wizard.setConnectionConfig(connectionConfig);
                wizard.setSourceConfig(sourceConfig);

                WizardDialog dialog = new WizardDialog(serversView.getSite().getShell(), wizard);
                dialog.setPageSize(600, 300);
                rc = dialog.open();

            } else if ("LDAP".equals(adapterName)) {
                wizard = new LDAPSourceWizard();
                wizard.setServer(server);
                wizard.setPartitionName(partitionName);
                wizard.setConnectionConfig(connectionConfig);
                wizard.setSourceConfig(sourceConfig);

                WizardDialog dialog = new WizardDialog(serversView.getSite().getShell(), wizard);
                dialog.setPageSize(600, 300);
                rc = dialog.open();

            } else if ("NIS".equals(adapterName)) {
                wizard = new NISSourceWizard();
                wizard.setServer(server);
                wizard.setPartitionName(partitionName);
                wizard.setConnectionConfig(connectionConfig);
                wizard.setSourceConfig(sourceConfig);

                WizardDialog dialog = new WizardDialog(serversView.getSite().getShell(), wizard);
                dialog.setPageSize(600, 300);
                rc = dialog.open();
            }

            if (rc == Window.CANCEL) return;

            serversView.open(node);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
	}
	
}