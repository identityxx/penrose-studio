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
import org.safehaus.penrose.studio.source.action.NewSourceAction;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceConfigs;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class SourcesNode extends Node {

    Logger log = Logger.getLogger(getClass());

    public final static char SEPARATOR = '.';

    ObjectsView view;

    private PartitionConfig partitionConfig;
    private String path;

    public SourcesNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new NewSourceAction(this));

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    paste();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
    }

    public void paste() throws Exception {

        Object newObject = view.getClipboard();

        if (!(newObject instanceof SourceConfig)) return;

        SourceConfig newSourceDefinition = (SourceConfig)((SourceConfig)newObject).clone();

        int counter = 1;
        String name = newSourceDefinition.getName();
        SourceConfigs sources = partitionConfig.getSourceConfigs();
        while (sources.getSourceConfig(name) != null) {
            counter++;
            name = newSourceDefinition.getName()+" ("+counter+")";
        }

        newSourceDefinition.setName(name);
        sources.addSourceConfig(newSourceDefinition);

        view.setClipboard(null);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        return !getChildren().isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        Map<String,Node> children = new LinkedHashMap<String,Node>();

        for (SourceConfig sourceConfig : partitionConfig.getSourceConfigs().getSourceConfigs()) {
            String sourceName = sourceConfig.getName();

            // log.debug("Checking "+ path +" with "+sourceName);
            if (path != null && !sourceName.startsWith(path + SEPARATOR)) continue;

            int p = sourceName.indexOf(SEPARATOR, path == null ? 0 : path.length() + 1);

            if (p >= 0) { // intermediate node
                String label = sourceName.substring(path == null ? 0 : path.length() + 1, p);

                if (!children.containsKey(label)) {
                    // log.debug("Creating sources node "+label);
                    SourcesNode sourcesNode = new SourcesNode(
                            view,
                            label,
                            ObjectsView.SOURCES,
                            PenrosePlugin.getImage(PenroseImage.FOLDER),
                            ObjectsView.SOURCES,
                            this
                    );

                    sourcesNode.setPartitionConfig(this.partitionConfig);
                    sourcesNode.setPath(sourceName.substring(0, p));

                    children.put(label, sourcesNode);
                }

            } else { // leaf node
                String label = sourceName.substring(path == null ? 0 : path.length() + 1);

                // log.debug("Creating source node "+label);
                SourceNode sourceNode = new SourceNode(
                        view,
                        label,
                        ObjectsView.SOURCE,
                        PenrosePlugin.getImage(PenroseImage.SOURCE),
                        sourceConfig,
                        this
                );

                sourceNode.setPartitionConfig(this.partitionConfig);
                sourceNode.setSourceConfig(sourceConfig);

                children.put(label, sourceNode);
            }
        }

        return children.values();
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
