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
package org.safehaus.penrose.studio.server;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.apache.log4j.Logger;

public class ServersLabelProvider extends LabelProvider implements ITableLabelProvider {

    protected Logger log = Logger.getLogger(getClass());

    ServersView view;

    public ServersLabelProvider(ServersView view) {
        this.view = view;
    }

    public void dispose() {
    }

    public String getColumnText(Object obj, int index) {
        return getText(obj);
    }

    public Image getColumnImage(Object obj, int index) {
        return getImage(obj);
    }

    public Image getImage(Object object) {
        Node node = (Node)object;
        Image image = node.getImage();
/*
        if (image == null) {
            image = PenroseStudio.getImage(PenroseImage.FOLDER);
        }
*/
        return image;
    }

    public String getText(Object obj) {
        Node node = (Node)obj;
        return node.getName();
    }
}
