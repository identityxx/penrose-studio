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
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.partition.PartitionNode;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.directory.action.*;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.partition.PartitionConfig;
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

    protected ServersView view;
    protected ProjectNode projectNode;
    protected PartitionsNode partitionsNode;
    protected PartitionNode partitionNode;

    private PartitionConfig partitionConfig;

    public DirectoryNode(String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        partitionNode = (PartitionNode)parent;
        partitionsNode = partitionNode.getPartitionsNode();
        projectNode = partitionsNode.getProjectNode();
        view = projectNode.getServersView();
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new NewRootEntryAction(this));

        //PenroseStudio penroseStudio = PenroseStudio.getInstance();
        //PenroseStudioWorkbenchAdvisor workbenchAdvisor = penroseStudio.getWorkbenchAdvisor();
        //PenroseStudioWorkbenchWindowAdvisor workbenchWindowAdvisor = workbenchAdvisor.getWorkbenchWindowAdvisor();
        //PenroseStudioActionBarAdvisor actionBarAdvisor = workbenchWindowAdvisor.getActionBarAdvisor();

        //if (actionBarAdvisor.getShowCommercialFeaturesAction().isChecked()) {
            manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            manager.add(new MapLDAPTreeFromTopAction(this));
            manager.add(new MapRootDSEAction(this));
            manager.add(new MapADSchemaAction(this));
            manager.add(new CreateLDAPSnapshotEntryAction(this));
        //}
    }

    public boolean hasChildren() throws Exception {
        return !partitionConfig.getDirectoryConfigs().getRootEntryMappings().isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        Collection rootEntryMappings = partitionConfig.getDirectoryConfigs().getRootEntryMappings();
        for (Iterator i=rootEntryMappings.iterator(); i.hasNext(); ) {
            EntryMapping entryMapping = (EntryMapping)i.next();

            String dn;
            if (entryMapping.getDn().isEmpty()) {
                dn = "Root DSE";
            } else {
                dn = entryMapping.getDn().toString();
            }

            EntryNode entryNode = new EntryNode(
                    dn,
                    ServersView.ENTRY,
                    PenrosePlugin.getImage(PenroseImage.HOME_NODE),
                    entryMapping,
                    this
            );

            entryNode.setPartitionConfig(this.partitionConfig);
            entryNode.setEntryMapping(entryMapping);

            children.add(entryNode);
        }

        return children;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public ServersView getView() {
        return view;
    }

    public void setView(ServersView view) {
        this.view = view;
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }

    public PartitionsNode getPartitionsNode() {
        return partitionsNode;
    }

    public void setPartitionsNode(PartitionsNode partitionsNode) {
        this.partitionsNode = partitionsNode;
    }

    public PartitionNode getPartitionNode() {
        return partitionNode;
    }

    public void setPartitionNode(PartitionNode partitionNode) {
        this.partitionNode = partitionNode;
    }
}
