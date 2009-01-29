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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.SWT;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.mapping.MappingConfig;
import org.safehaus.penrose.mapping.MappingConfigManager;
import org.safehaus.penrose.mapping.MappingReader;
import org.safehaus.penrose.mapping.MappingManagerClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.mapping.MappingsNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.ServersView;

import java.io.File;

public class ImportMappingsAction extends Action {

    Logger log = Logger.getLogger(getClass());

    MappingsNode node;

	public ImportMappingsAction(MappingsNode node) {
        this.node = node;

        setText("Import");
        setId(getClass().getName());
	}

	public void run() {
        try {
            ServersView serversView = ServersView.getInstance();
            Server server = node.getProjectNode().getServer();

            FileDialog dialog = new FileDialog(serversView.getSite().getShell(), SWT.OPEN);
            dialog.setText("Import Mappings");
            dialog.setFileName("mappings.xml");

            String filename = dialog.open();
            if (filename == null) return;

            File file = new File(filename);

            MappingConfigManager mappingConfigManager = new MappingConfigManager();

            MappingReader mappingReader = new MappingReader();
            mappingReader.read(file, mappingConfigManager);

            PartitionClient partitionClient = server.getClient().getPartitionManagerClient().getPartitionClient(node.getPartitionName());
            MappingManagerClient mappingManagerClient = partitionClient.getMappingManagerClient();

            for (MappingConfig mappingConfig : mappingConfigManager.getMappingConfigs()) {
                mappingManagerClient.createMapping(mappingConfig);
            }

            partitionClient.store();

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

            serversView.open(node);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
	}

}