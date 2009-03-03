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
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.studio.server.Server;

/**
 * @author Endi S. Dewata
 */
public class BuiltInSchemasNode extends SchemasNode {

    Logger log = Logger.getLogger(getClass());

    protected SchemasNode schemasNode;

    public BuiltInSchemasNode(String name, SchemasNode schemasNode) {
        super(name, schemasNode.getServerNode());

        this.schemasNode = schemasNode;
    }

    public void update() throws Exception {

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();

        for (String schemaName : schemaManagerClient.getBuiltInSchemaNames()) {

            SchemaNode schemaNode = new SchemaNode(
                    schemaName,
                    this
            );

            addChild(schemaNode);
        }
    }

    public SchemasNode getSchemasNode() {
        return schemasNode;
    }

    public void setSchemasNode(SchemasNode schemasNode) {
        this.schemasNode = schemasNode;
    }
}