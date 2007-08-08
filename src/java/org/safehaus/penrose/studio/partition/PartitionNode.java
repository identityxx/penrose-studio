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
import org.safehaus.penrose.studio.PenroseStudio;
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
import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class PartitionNode extends Node {

    public Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    private PartitionConfig partitionConfig;

    Action copyAction;

    public PartitionNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
        partitionConfig = (PartitionConfig)object;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Close") {
            public void run() {
                try {
                    close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            public boolean isEnabled() {
                return partitionConfig != null;
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Save") {
            public void run() {
                try {
                    save();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            public boolean isEnabled() {
                return partitionConfig != null;
            }
        });

        manager.add(new Action("Upload") {
            public void run() {
                try {
                    upload();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            public boolean isEnabled() {
                return partitionConfig != null;
            }
        });

        manager.add(new ExportPartitionAction(this));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));


        manager.add(new Action("Copy") {
            public void run() {
                try {
                    copy();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            public boolean isEnabled() {
                return partitionConfig != null;
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            public boolean isEnabled() {
                Object object = view.getClipboard();
                return object != null && object instanceof PartitionConfig;
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {
        load();
    }

    public void load() throws Exception {
        log.debug("Loading "+name+" partition.");

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        File workDir = penroseStudio.getWorkDir();
        File dir = new File(workDir, "partitions"+File.separator+name);

        penroseStudio.downloadFolder(workDir, "partitions"+File.separator+name);

        PartitionConfigs partitionConfigs = penroseStudio.getPartitionConfigs();
        partitionConfig = partitionConfigs.load(dir);
    }

    public void save() throws Exception {
        log.debug("Saving "+name+" partition.");

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PartitionConfigs partitionConfigs = penroseStudio.getPartitionConfigs();
        
        File workDir = penroseStudio.getWorkDir();
        partitionConfigs.store(workDir, partitionConfig);
    }

    public void upload() throws Exception {
        log.debug("Uploading "+name+" partition.");

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        
        File workDir = penroseStudio.getWorkDir();
        penroseStudio.uploadFolder("partitions/"+name);
    }

    public void close() throws Exception {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        PartitionConfigs partitionConfigs = penroseStudio.getPartitionConfigs();
        partitionConfigs.removePartitionConfig(partitionConfig.getName());

        partitionConfig = null;

        penroseStudio.notifyChangeListeners();
    }

    public void remove() throws Exception {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        boolean confirm = MessageDialog.openQuestion(
                shell,
                "Confirmation",
                "Remove Partition \""+partitionConfig.getName()+"\"?");

        if (!confirm) return;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        PartitionConfigs partitionConfigs = penroseStudio.getPartitionConfigs();
        partitionConfigs.removePartitionConfig(partitionConfig.getName());

        partitionConfig = null;

        penroseStudio.notifyChangeListeners();
    }

    public void copy() throws Exception {
        log.debug("Copying "+name+" partition.");
        view.setClipboard(partitionConfig);
    }

    public void paste() throws Exception {
        PartitionsNode partitionsNode = (PartitionsNode)parent;
        partitionsNode.paste();
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {

        if (partitionConfig == null) {
            load();
        }

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
