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
package org.safehaus.penrose.studio.source;

import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.source.action.NewSourceAction;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.SourceConfig;
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
public class SourcesNode extends Node {

    Logger log = Logger.getLogger(getClass());

    Server server;

    private Partition partition;

    public SourcesNode(
            Server server,
            String name,
            Image image,
            Object object,
            Node parent
    ) {
        super(name, image, object, parent);
        this.server = server;
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(new NewSourceAction(this));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getPasteAction());
    }

    public boolean canPaste(Object object) throws Exception {
        return object instanceof SourceConfig;
    }

    public void paste(Object object) throws Exception {
        SourceConfig sourceConfig = (SourceConfig)object;

        int counter = 1;
        String name = sourceConfig.getName();

        while (partition.getSourceConfig(name) != null) {
            counter++;
            name = sourceConfig.getName()+" ("+counter+")";
        }

        sourceConfig.setName(name);
        partition.addSourceConfig(sourceConfig);
    }

    public boolean hasChildren() throws Exception {
        return !partition.getSourceConfigs().isEmpty();
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        Collection sourceConfigs = partition.getSourceConfigs();
        for (Iterator i=sourceConfigs.iterator(); i.hasNext(); ) {
            SourceConfig sourceConfig = (SourceConfig)i.next();

            SourceNode sourceNode = new SourceNode(
                    server,
                    sourceConfig.getName(),
                    PenrosePlugin.getImage(PenroseImage.SOURCE),
                    sourceConfig,
                    this
            );

            sourceNode.setPartition(partition);
            sourceNode.setSourceConfig(sourceConfig);

            children.add(sourceNode);
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
