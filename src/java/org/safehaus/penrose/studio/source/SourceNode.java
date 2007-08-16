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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.swt.graphics.Image;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.partition.PartitionNode;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.plugin.PluginManager;
import org.safehaus.penrose.studio.plugin.Plugin;
import org.safehaus.penrose.studio.source.editor.*;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class SourceNode extends Node {

    Logger log = Logger.getLogger(getClass());

    protected ServersView view;
    protected ProjectNode projectNode;
    protected PartitionsNode partitionsNode;
    protected PartitionNode partitionNode;
    protected SourcesNode sourcesNode;

    private PartitionConfig partitionConfig;
    private ConnectionConfig connectionConfig;
    private SourceConfig sourceConfig;

    public SourceNode(String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        sourcesNode = (SourcesNode)parent;
        partitionNode = sourcesNode.getPartitionNode();
        partitionsNode = partitionNode.getPartitionsNode();
        projectNode = partitionsNode.getProjectNode();
        view = projectNode.getServersView();
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

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Copy") {
            public void run() {
                try {
                    copy();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
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
        });

        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.SIZE_16x16, PenroseImage.DELETE)) {
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

        ConnectionConfig con = partitionConfig.getConnectionConfigs().getConnectionConfig(sourceConfig.getConnectionName());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PluginManager pluginManager = penroseStudio.getPluginManager();
        Plugin plugin = pluginManager.getPlugin(con.getAdapterName());

        SourceEditorInput sei = plugin.createSourceEditorInput();
        sei.setProject(projectNode.getProject());
        sei.setPartitionConfig(this.partitionConfig);
        sei.setSourceConfig(sourceConfig);

        String sourceEditorClassName = plugin.getSourceEditorClass();

        log.debug("Opening "+sourceEditorClassName);
        page.openEditor(sei, sourceEditorClassName);
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Confirmation", "Remove selected sources?");

        if (!confirm) return;

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof SourceNode)) continue;

            SourceNode sourceNode = (SourceNode)node;
            SourceConfig sourceConfig = sourceNode.getSourceConfig();
            partitionConfig.getSourceConfigs().removeSourceConfig(sourceConfig.getName());
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void copy() throws Exception {
        view.setClipboard(getObject());
    }

    public void paste() throws Exception {

        Object newObject = view.getClipboard();

        if (!(newObject instanceof SourceConfig)) return;

        SourceConfig newSourceDefinition = (SourceConfig)((SourceConfig)newObject).clone();

        int counter = 1;
        String name = newSourceDefinition.getName();
        while (partitionConfig.getSourceConfigs().getSourceConfig(name) != null) {
            counter++;
            name = newSourceDefinition.getName()+" ("+counter+")";
        }

        newSourceDefinition.setName(name);
        partitionConfig.getSourceConfigs().addSourceConfig(newSourceDefinition);

        view.setClipboard(null);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }
}
