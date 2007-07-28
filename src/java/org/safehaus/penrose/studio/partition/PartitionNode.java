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
package org.safehaus.penrose.studio.partition;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Image;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.partition.action.ExportPartitionAction;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.connection.ConnectionsNode;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.module.ModulesNode;
import org.safehaus.penrose.studio.source.SourcesNode;
import org.safehaus.penrose.studio.directory.DirectoryNode;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class PartitionNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    private PartitionConfig partitionConfig;

    public PartitionNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new ExportPartitionAction(partitionConfig));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

/*
        manager.add(new Action("Copy") {
            public void run() {
                try {
                    copy();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
*/
        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
    }

    public void remove() throws Exception {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        boolean confirm = MessageDialog.openQuestion(
                shell,
                "Confirmation",
                "Remove Partition \""+partitionConfig.getName()+"\"?");

        if (!confirm) return;

        PenroseApplication penroseApplication = PenroseApplication.getInstance();

        PartitionConfigs partitionConfigs = penroseApplication.getPartitionConfigs();
        partitionConfigs.removePartitionConfig(partitionConfig.getName());

        penroseApplication.notifyChangeListeners();
    }

    public void copy() throws Exception {
        view.setClipboard(partitionConfig);
    }

    public void paste() throws Exception {

        Object object = view.getClipboard();
        if (!(object instanceof PartitionConfig)) return;

        PartitionConfig oldPartition = (PartitionConfig)object;
        PartitionConfig newPartition = (PartitionConfig)oldPartition.clone();

        String originalName = newPartition.getName();

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PartitionConfigs partitionConfigs = penroseApplication.getPartitionConfigs();

        int counter = 1;
        String name = originalName+" ("+counter+")";
        while (partitionConfigs.getPartitionConfig(name) != null) {
            counter++;
            name = originalName+" ("+counter+")";
        }

        newPartition.setName(name);

        partitionConfigs.addPartitionConfig(newPartition);

        view.setClipboard(null);
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        DirectoryNode directoryNode = new DirectoryNode(
                view,
                ObjectsView.DIRECTORY,
                ObjectsView.DIRECTORY,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.DIRECTORY,
                this
        );

        directoryNode.setPartitionConfig(partitionConfig);

        children.add(directoryNode);

        ConnectionsNode connectionsNode = new ConnectionsNode(
                view,
                ObjectsView.CONNECTIONS,
                ObjectsView.CONNECTIONS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                partitionConfig,
                this
        );

        connectionsNode.setPartitionConfig(partitionConfig);

        children.add(connectionsNode);

        SourcesNode sourcesNode = new SourcesNode(
                view,
                ObjectsView.SOURCES,
                ObjectsView.SOURCES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.SOURCES,
                this
        );

        sourcesNode.setPartitionConfig(partitionConfig);

        children.add(sourcesNode);

        ModulesNode modulesNode = new ModulesNode(
                view,
                ObjectsView.MODULES,
                ObjectsView.MODULES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.MODULES,
                this
        );

        modulesNode.setPartitionConfig(partitionConfig);

        children.add(modulesNode);

        return children;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }
}
