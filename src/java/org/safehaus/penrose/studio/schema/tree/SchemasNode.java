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
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class SchemasNode extends Node {

    Logger log = Logger.getLogger(getClass());

    protected ServersView view;
    protected ServerNode serverNode;

    private BuiltInSchemasNode builtInSchemasNode;
    private CustomSchemasNode customSchemasNode;

    public SchemasNode(String name, ServerNode serverNode) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, serverNode);
        this.serverNode = serverNode;
        this.view = serverNode.getServersView();
    }

    public void update() throws Exception {

        builtInSchemasNode = new BuiltInSchemasNode(
                "Built-in Schemas",
                this
        );

        builtInSchemasNode.init();

        addChild(builtInSchemasNode);

        customSchemasNode = new CustomSchemasNode(
                "Custom Schemas",
                this
        );

        customSchemasNode.init();

        addChild(customSchemasNode);
    }

    public void expand() throws Exception {
        if (children == null) update();
    }

    public void refresh() throws Exception {
        removeChildren();
        update();
    }

    public ServerNode getServerNode() {
        return serverNode;
    }

    public void setServerNode(ServerNode serverNode) {
        this.serverNode = serverNode;
    }

    public BuiltInSchemasNode getBuiltInSchemasNode() {
        return builtInSchemasNode;
    }

    public void setBuiltInSchemasNode(BuiltInSchemasNode builtInSchemasNode) {
        this.builtInSchemasNode = builtInSchemasNode;
    }

    public CustomSchemasNode getCustomSchemasNode() {
        return customSchemasNode;
    }

    public void setCustomSchemasNode(CustomSchemasNode customSchemasNode) {
        this.customSchemasNode = customSchemasNode;
    }
}
