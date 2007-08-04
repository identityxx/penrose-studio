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
package org.safehaus.penrose.studio.engine;

import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.engine.EngineConfig;
import org.eclipse.swt.graphics.Image;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class EnginesNode extends Node {

    ObjectsView view;

    public EnginesNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Collection engineConfigs = penroseStudio.getPenroseConfig().getEngineConfigs();

        for (Iterator i=engineConfigs.iterator(); i.hasNext(); ) {
            EngineConfig engineConfig = (EngineConfig)i.next();

            children.add(new EngineNode(
                    view,
                    engineConfig.getName(),
                    ObjectsView.ENGINE,
                    PenrosePlugin.getImage(PenroseImage.ENGINE),
                    engineConfig,
                    this
            ));
        }

        return children;
    }
}
