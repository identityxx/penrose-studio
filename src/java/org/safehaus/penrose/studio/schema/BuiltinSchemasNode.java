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
package org.safehaus.penrose.studio.schema;

import org.apache.log4j.Logger;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class BuiltinSchemasNode extends SchemasNode {

    Logger log = Logger.getLogger(getClass());

    protected SchemasNode schemasNode;

    public BuiltinSchemasNode(String name, String type, Object object, SchemasNode schemasNode) {
        super(name, object, schemasNode.getProjectNode());

        this.schemasNode = schemasNode;
    }

    public boolean hasChildren() throws Exception {
        return !getChildren().isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        Project project = projectNode.getProject();
        PenroseClient client = project.getClient();
        SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();

        for (String schemaName : schemaManagerClient.getBuiltInSchemaNames()) {

            SchemaNode schemaNode = new SchemaNode(
                    schemaName,
                    PenroseStudio.getImage(PenroseImage.SCHEMA),
                    schemaName,
                    this
            );

            children.add(schemaNode);
        }

        return children;
    }

    public ServersView getView() {
        return view;
    }

    public void setView(ServersView view) {
        this.view = view;
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }

    public SchemasNode getSchemasNode() {
        return schemasNode;
    }

    public void setSchemasNode(SchemasNode schemasNode) {
        this.schemasNode = schemasNode;
    }
}