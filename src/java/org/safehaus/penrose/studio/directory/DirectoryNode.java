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
package org.safehaus.penrose.studio.directory;

import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.directory.action.*;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.partition.Partition;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class DirectoryNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private Server server;
    private Partition partition;

    public DirectoryNode(
            String name,
            Image image,
            Object object,
            Node parent
    ) {
        super(name, image, object, parent);
    }

    public void showMenu(IMenuManager manager) throws Exception {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(new NewRootEntryAction(this));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        manager.add(new MapLDAPTreeFromTopAction(this));
        manager.add(new MapRootDSEAction(this));
        manager.add(new MapADSchemaAction(this));
        manager.add(new CreateLDAPSnapshotEntryAction(this));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getPasteAction());
    }


    public boolean canPaste(Object object) throws Exception {
        return object instanceof EntryMapping;
    }

    public void paste(Object object) throws Exception {
        EntryMapping newEntryMapping = (EntryMapping)object;
        newEntryMapping.setParentDn(null);

        partition.addEntryMapping(newEntryMapping);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();
    }

    public boolean hasChildren() throws Exception {
        return !partition.getRootEntryMappings().isEmpty();
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        Collection rootEntryMappings = partition.getRootEntryMappings();
        for (Iterator i=rootEntryMappings.iterator(); i.hasNext(); ) {
            EntryMapping entryMapping = (EntryMapping)i.next();

            String dn = entryMapping.getDn();
            if ("".equals(dn)) dn = "Root DSE";

            EntryNode entryNode = new EntryNode(
                    server,
                    dn,
                    PenrosePlugin.getImage(PenroseImage.HOME_NODE),
                    entryMapping,
                    this
            );

            entryNode.setPartition(partition);
            entryNode.setEntryMapping(entryMapping);

            children.add(entryNode);
        }

        return children;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
