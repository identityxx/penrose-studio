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

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.management.partition.PartitionClient;
import org.safehaus.penrose.management.partition.PartitionManagerClient;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.management.source.SourceClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.partition.PartitionNode;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.plugin.Plugin;
import org.safehaus.penrose.studio.plugin.PluginManager;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.source.editor.SourceEditorInput;
import org.safehaus.penrose.studio.tree.Node;

import java.util.Collection;

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

    private String partitionName;
    private String adapterName;
    private String sourceName;

    public SourceNode(String name, Image image, Object object, Object parent) {
        super(name, image, object, parent);
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

        manager.add(new Action("Delete", PenroseStudioPlugin.getImageDescriptor(PenroseImage.SIZE_16x16, PenroseImage.DELETE)) {
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

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PluginManager pluginManager = penroseStudio.getPluginManager();
        Plugin plugin = pluginManager.getPlugin(adapterName);

        SourceEditorInput ei = plugin.createSourceEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setPartitionName(partitionName);
        ei.setSourceName(sourceName);

        String sourceEditorClassName = plugin.getSourceEditorClass();

        log.debug("Opening "+sourceEditorClassName);
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, sourceEditorClassName);
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Confirmation", "Remove selected sources?");

        if (!confirm) return;

        Project project = projectNode.getProject();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        //SourceConfigManager sourceConfigManager = partitionConfig.getSourceConfigManager();

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof SourceNode)) continue;

            SourceNode sourceNode = (SourceNode)node;
            partitionClient.removeSource(sourceNode.getSourceName());
        }

        partitionClient.store();
        //project.save(partitionConfig, sourceConfigManager);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void copy() throws Exception {

        Project project = projectNode.getProject();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        SourceClient sourceClient = partitionClient.getSourceClient(sourceName);
        view.setClipboard(sourceClient.getSourceConfig());
    }

    public void paste() throws Exception {

        Object newObject = view.getClipboard();

        if (!(newObject instanceof SourceConfig)) return;

        Project project = projectNode.getProject();

        SourceConfig newSourceConfig = (SourceConfig)((SourceConfig)newObject).clone();
        view.setClipboard(null);
/*
        SourceConfigManager sourceConfigManager = partitionConfig.getSourceConfigManager();
        int counter = 1;
        String name = newSourceConfig.getName();
        while (sourceConfigManager.getSourceConfig(name) != null) {
            counter++;
            name = newSourceConfig.getName()+" ("+counter+")";
        }
        newSourceConfig.setName(name);

        sourceConfigManager.addSourceConfig(newSourceConfig);
        project.save(partitionConfig, sourceConfigManager);
*/
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        Collection<String> sourceNames = partitionClient.getSourceNames();
        int counter = 1;
        String name = newSourceConfig.getName();
        while (sourceNames.contains(name)) {
            counter++;
            name = newSourceConfig.getName()+" ("+counter+")";
        }
        newSourceConfig.setName(name);

        partitionClient.createSource(newSourceConfig);
        partitionClient.store();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }
}
