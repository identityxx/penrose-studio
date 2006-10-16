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
package org.safehaus.penrose.studio.object;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.apache.log4j.Logger;

import java.util.Collection;

public class ObjectsContentProvider implements ITreeContentProvider {

    protected Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    public ObjectsContentProvider(ObjectsView view) {
        this.view = view;
    }

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    }

    public void dispose() {
    }

    public Object[] getChildren(Object element) {
        try {
            Node node = (Node)element;
            
            Collection children = node.getChildren();
            if (children == null) return new Object[0];

            return node.getChildren().toArray();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }

        return new Object[0];
    }

    public Object[] getElements(Object inputElement) {
        return view.nodes.toArray();
    }

    public Object getParent(Object element) {
        Node node = (Node)element;
        return node.getParent();
    }

    public boolean hasChildren(Object element) {
        try {
            Node node = (Node)element;
            return node.hasChildren();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }

        return false;
    }
}
