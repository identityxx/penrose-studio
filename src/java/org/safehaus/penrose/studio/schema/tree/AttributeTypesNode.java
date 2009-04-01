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
package org.safehaus.penrose.studio.schema.tree;

import org.eclipse.swt.graphics.Image;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.schema.SchemaClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.schema.SchemaConfig;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class AttributeTypesNode extends Node {

    ServersView view;
    ServerNode serverNode;
    SchemaNode schemaNode;

    private SchemaConfig schemaConfig;

    public AttributeTypesNode(ServersView view, String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
        schemaNode = (SchemaNode)parent;
        serverNode = schemaNode.getServerNode();
        this.view = serverNode.getServersView();
    }

    public void update() throws Exception {

        Server server = serverNode.getServer();
        PenroseClient client = server.getClient();
        SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
        SchemaClient schemaClient = schemaManagerClient.getSchemaClient(schemaConfig.getName());

        Collection<AttributeType> attributeTypes = schemaClient.getAttributeTypes();
        for (AttributeType attributeType : attributeTypes) {
            addChild(new AttributeTypeNode(
                    view,
                    attributeType.getName(),
                    PenroseStudio.getImage(PenroseImage.ATTRIBUTE_TYPE),
                    attributeType,
                    this
            ));
        }
    }

    public SchemaConfig getSchemaConfig() {
        return schemaConfig;
    }

    public void setSchemaConfig(SchemaConfig schemaConfig) {
        this.schemaConfig = schemaConfig;
    }
}
