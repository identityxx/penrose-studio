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

import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.schema.ObjectClass;
import org.safehaus.penrose.schema.SchemaConfig;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.schema.Schema;
import org.eclipse.swt.graphics.Image;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class ObjectClassesNode extends Node {

    ObjectsView view;

    private SchemaConfig schemaConfig;

    public ObjectClassesNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public boolean hasChildren() throws Exception {
        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        SchemaManager schemaManager = penroseApplication.getSchemaManager();
        Schema schema = schemaManager.getSchema(schemaConfig.getName());
        return !schema.getObjectClasses().isEmpty();
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        SchemaManager schemaManager = penroseApplication.getSchemaManager();
        Schema schema = schemaManager.getSchema(schemaConfig.getName());

        Collection objectClasses = schema.getObjectClasses();
        for (Iterator i=objectClasses.iterator(); i.hasNext(); ) {
            ObjectClass objectClass = (ObjectClass)i.next();

            children.add(new ObjectClassNode(
                    view,
                    objectClass.getName(),
                    ObjectsView.OBJECT_CLASS,
                    PenrosePlugin.getImage(PenroseImage.OBJECT_CLASS),
                    objectClass,
                    this
            ));
        }

        return children;
    }

    public SchemaConfig getSchemaConfig() {
        return schemaConfig;
    }

    public void setSchemaConfig(SchemaConfig schemaConfig) {
        this.schemaConfig = schemaConfig;
    }
}
