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
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.partition.PartitionNode;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.source.action.NewSourceAction;
import org.safehaus.penrose.studio.tree.Node;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class SourcesNode extends Node {

    Logger log = Logger.getLogger(getClass());

    public final static char SEPARATOR = '_';

    protected ServersView view;
    protected ProjectNode projectNode;
    protected PartitionsNode partitionsNode;
    protected PartitionNode partitionNode;

    private String partitionName;
    private String path;

    public SourcesNode(String name, Image image, Object object, Object parent) {
        super(name, image, object, parent);

        if (parent instanceof PartitionNode) {
            partitionNode = (PartitionNode)parent;

        } else if (parent instanceof SourcesNode) {
            partitionNode = ((SourcesNode)parent).getPartitionNode();
        }

        partitionsNode = partitionNode.getPartitionsNode();
        projectNode = partitionsNode.getProjectNode();
        view = projectNode.getServersView();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new NewSourceAction(this));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
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
        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

        Collection<String> sourceNames = sourceManagerClient.getSourceNames();
        int counter = 1;
        String name = newSourceConfig.getName();
        while (sourceNames.contains(name)) {
            counter++;
            name = newSourceConfig.getName()+" ("+counter+")";
        }
        newSourceConfig.setName(name);

        sourceManagerClient.createSource(newSourceConfig);
        partitionClient.store();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        return !getChildren().isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        Map<String,Node> children = new TreeMap<String,Node>();

        Project project = projectNode.getProject();
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

        //log.debug("Getting sources:");

        for (String sourceName : sourceManagerClient.getSourceNames()) {
            //log.debug(" - "+sourceName);

            SourceClient sourceClient = sourceManagerClient.getSourceClient(sourceName);
            String adapterName = sourceClient.getAdapterName();

            // log.debug("Checking "+ path +" with "+sourceName);
            if (path != null && !sourceName.startsWith(path + SEPARATOR)) continue;

            int p = sourceName.indexOf(SEPARATOR, path == null ? 0 : path.length() + 1);

            if (p >= 0) { // intermediate node
                String label = sourceName.substring(path == null ? 0 : path.length() + 1, p);

                if (!children.containsKey(label)) {
                    // log.debug("Creating sources node "+label);
                    SourcesNode sourcesNode = new SourcesNode(
                            label,
                            PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                            ServersView.SOURCES,
                            this
                    );

                    sourcesNode.setPartitionName(partitionName);
                    sourcesNode.setPath(sourceName.substring(0, p));

                    children.put(label, sourcesNode);
                }

            } else { // leaf node
                String label = sourceName.substring(path == null ? 0 : path.length() + 1);

                // log.debug("Creating source node "+label);
                SourceNode sourceNode = new SourceNode(
                        label,
                        PenroseStudioPlugin.getImage(PenroseImage.SOURCE),
                        sourceName,
                        this
                );

                sourceNode.setPartitionName(partitionName);
                sourceNode.setAdapterName(adapterName);
                sourceNode.setSourceName(sourceName);

                children.put(label, sourceNode);
            }
        }

        return children.values();
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
