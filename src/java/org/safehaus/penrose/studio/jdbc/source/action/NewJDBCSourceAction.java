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
package org.safehaus.penrose.studio.jdbc.source.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.jdbc.source.wizard.JDBCSourceWizard;
import org.safehaus.penrose.studio.source.SourcesNode;
import org.safehaus.penrose.source.SourceConfig;
import org.apache.log4j.Logger;

public class NewJDBCSourceAction extends Action {

    Logger log = Logger.getLogger(getClass());

    SourcesNode sourcesNode;

	public NewJDBCSourceAction(SourcesNode node) {
        this.sourcesNode = node;

        setText("New JDBC Source...");
        setId(getClass().getName());
	}

	public void run() {
        try {
            ServersView serversView = ServersView.getInstance();
            Server server = sourcesNode.getServerNode().getServer();

            SourceConfig sourceConfig = new SourceConfig();

            JDBCSourceWizard wizard = new JDBCSourceWizard();
            wizard.setServer(server);
            wizard.setPartitionName(sourcesNode.getPartitionName());
            wizard.setSourceConfig(sourceConfig);

            WizardDialog dialog = new WizardDialog(serversView.getSite().getShell(), wizard);
            dialog.setPageSize(600, 300);
            dialog.open();

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

            serversView.open(sourcesNode);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
	}

}