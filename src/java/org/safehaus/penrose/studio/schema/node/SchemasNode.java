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
package org.safehaus.penrose.studio.schema.node;

import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class SchemasNode extends Node {

    Logger log = Logger.getLogger(getClass());

    protected ServersView view;
    protected ServerNode projectNode;

    public SchemasNode(String name, Object object, ServerNode projectNode) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), object, projectNode);

        this.projectNode = projectNode;
        this.view = this.projectNode.getServersView();
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        BuiltinSchemasNode builtinSchemas = new BuiltinSchemasNode(
                "Built-in Schemas",
                ServersView.BUILTIN_SCHEMA,
                PenroseStudio.getImage(PenroseImage.SCHEMA),
                this
        );

        children.add(builtinSchemas);

        CustomSchemasNode customSchemasNode = new CustomSchemasNode(
                "Custom Schemas",
                ServersView.CUSTOM_SCHEMA,
                PenroseStudio.getImage(PenroseImage.SCHEMA),
                this
        );

        children.add(customSchemasNode);

        return children;
    }

    public ServersView getView() {
        return view;
    }

    public void setView(ServersView view) {
        this.view = view;
    }

    public ServerNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ServerNode projectNode) {
        this.projectNode = projectNode;
    }
}
