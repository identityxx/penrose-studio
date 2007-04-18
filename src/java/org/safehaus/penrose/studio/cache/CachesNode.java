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
package org.safehaus.penrose.studio.cache;

import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.cache.CacheConfig;
import org.eclipse.swt.graphics.Image;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class CachesNode extends Node {

    ObjectsView view;

    public CachesNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
/*
        CacheConfig entryCacheConfig = penroseApplication.getPenroseConfig().getEntryCacheConfig();
        if (entryCacheConfig != null) {
            children.add(new CacheNode(
                    view,
                    ObjectsView.ENTRY_CACHE,
                    ObjectsView.ENTRY_CACHE,
                    PenrosePlugin.getImage(PenroseImage.CACHE),
                    entryCacheConfig,
                    this
            ));
        }

        CacheConfig sourceCacheConfig = penroseApplication.getPenroseConfig().getSourceCacheConfig();
        if (sourceCacheConfig != null) {
            children.add(new CacheNode(
                    view,
                    ObjectsView.SOURCE_CACHE,
                    ObjectsView.SOURCE_CACHE,
                    PenrosePlugin.getImage(PenroseImage.CACHE),
                    sourceCacheConfig,
                    this
            ));
        }
*/
        return children;
    }
}
