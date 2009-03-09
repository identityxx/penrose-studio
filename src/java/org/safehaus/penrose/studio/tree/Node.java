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
package org.safehaus.penrose.studio.tree;

import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class Node {

    public final static Collection<Node> EMPTY_LIST = new ArrayList<Node>();

    public Logger log = Logger.getLogger(getClass());

    protected String name;
    protected Image image;
    protected Object object;
    protected Node parent;

    protected Collection<Node> children;

    public Node(String name, Image image, Object object, Node parent) {
        this.name = name;
        this.image = image;
        this.object = object;
        this.parent = parent;
    }

    public void init() throws Exception {
    }

    public void refresh() throws Exception {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public int hashCode() {
        return (name == null ? 0 : name.hashCode()) +
                (image == null ? 0 : image.hashCode()) +
                (object == null ? 0 : object.hashCode()) +
                (parent == null ? 0 : parent.hashCode());
    }

    boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return o2.equals(o1);
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        if (object.getClass() != this.getClass()) return false;

        Node node = (Node)object;
        if (!equals(name, node.name)) return false;
        if (!equals(image, node.image)) return false;
        if (!equals(this.object, node.object)) return false;
        if (!equals(parent, node.parent)) return false;

        return true;
    }

    public void showMenu(IMenuManager manager) throws Exception {
    }

    public void open() throws Exception {
    }

    public void expand() throws Exception {
    }

    public void collapse() throws Exception {
    }

    public void close() throws Exception {
    }

    public boolean hasChildren() throws Exception {
        return children == null || !children.isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {
        return children == null ? EMPTY_LIST : children;
    }

    public void removeChildren() {
        if (children != null) children.clear();
    }

    public void addChild(Node node) {
        if (children == null) children = new ArrayList<Node>();
        children.add(node);
    }

    public String toString() {
        return name;
    }
}
