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
import org.safehaus.penrose.studio.directory.action.*;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.ldap.DN;
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

    ObjectsView view;

    private Partition partition;

    public DirectoryNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new NewRootEntryAction(this));

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseWorkbenchAdvisor workbenchAdvisor = penroseApplication.getWorkbenchAdvisor();
        PenroseWorkbenchWindowAdvisor workbenchWindowAdvisor = workbenchAdvisor.getWorkbenchWindowAdvisor();
        PenroseActionBarAdvisor actionBarAdvisor = workbenchWindowAdvisor.getActionBarAdvisor();

        //if (actionBarAdvisor.getShowCommercialFeaturesAction().isChecked()) {
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            manager.add(new MapLDAPTreeFromTopAction(this));
            manager.add(new MapRootDSEAction(this));
            manager.add(new MapADSchemaAction(this));
            manager.add(new CreateLDAPSnapshotEntryAction(this));
        //}
    }

    public boolean hasChildren() throws Exception {
        return !partition.getMappings().getRootEntryMappings().isEmpty();
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        Collection rootEntryMappings = partition.getMappings().getRootEntryMappings();
        for (Iterator i=rootEntryMappings.iterator(); i.hasNext(); ) {
            EntryMapping entryMapping = (EntryMapping)i.next();

            String dn;
            if (entryMapping.getDn().isEmpty()) {
                dn = "Root DSE";
            } else {
                dn = entryMapping.getDn().toString();
            }

            EntryNode entryNode = new EntryNode(
                    view,
                    dn,
                    ObjectsView.ENTRY,
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
}
