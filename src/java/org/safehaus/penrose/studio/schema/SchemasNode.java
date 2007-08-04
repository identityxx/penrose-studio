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

import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.schema.action.ImportSchemaAction;
import org.safehaus.penrose.studio.schema.action.NewSchemaAction;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.schema.SchemaConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class SchemasNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    public SchemasNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {
        manager.add(new NewSchemaAction());
        manager.add(new ImportSchemaAction());
    }

    public boolean hasChildren() throws Exception {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseConfig penroseConfig = penroseStudio.getPenroseConfig();
        return !penroseConfig.getSchemaConfigs().isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseConfig penroseConfig = penroseStudio.getPenroseConfig();

        Collection schemaConfigs = penroseConfig.getSchemaConfigs();
        for (Iterator i=schemaConfigs.iterator(); i.hasNext(); ) {
            SchemaConfig schemaConfig = (SchemaConfig)i.next();

            SchemaNode schemaNode = new SchemaNode(
                    view,
                    schemaConfig.getName(),
                    ObjectsView.SCHEMA,
                    PenrosePlugin.getImage(PenroseImage.SCHEMA),
                    schemaConfig,
                    this
            );

            schemaNode.setSchemaConfig(schemaConfig);

            children.add(schemaNode);
        }

        return children;
    }
}
