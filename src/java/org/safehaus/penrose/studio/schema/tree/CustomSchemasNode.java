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
package org.safehaus.penrose.studio.schema.tree;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.studio.action.RefreshAction;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.schema.action.ImportSchemaAction;
import org.safehaus.penrose.studio.schema.action.NewSchemaAction;

/**
 * @author Endi S. Dewata
 */
public class CustomSchemasNode extends SchemasNode {

    Logger log = Logger.getLogger(getClass());

    protected SchemasNode schemasNode;

    public CustomSchemasNode(String name, SchemasNode schemasNode) {
        super(name, schemasNode.getServerNode());

        this.schemasNode = schemasNode;
    }

    public void update() throws Exception {

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();

        for (String schemaName : schemaManagerClient.getCustomSchemaNames()) {

            SchemaNode schemaNode = new SchemaNode(
                    schemaName,
                    this
            );

            addChild(schemaNode);
        }
    }

    public void showMenu(IMenuManager manager) {
        manager.add(new NewSchemaAction());
        manager.add(new ImportSchemaAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new RefreshAction(this));
    }

    public SchemasNode getSchemasNode() {
        return schemasNode;
    }

    public void setSchemasNode(SchemasNode schemasNode) {
        this.schemasNode = schemasNode;
    }
}