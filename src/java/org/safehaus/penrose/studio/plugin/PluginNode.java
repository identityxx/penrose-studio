package org.safehaus.penrose.studio.plugin;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class PluginNode extends Node {

    protected Logger log = Logger.getLogger(getClass());

    private ServersView view;
    private ProjectNode projectNode;
    private PluginsNode pluginsNode;

    public PluginNode(String name, Object parent) {
        super(
                name,
                PenroseStudioPlugin.getImage(PenroseImage.MODULE),
                null,
                parent
        );

        pluginsNode = (PluginsNode)parent;
        projectNode = pluginsNode.getProjectNode();
        view = projectNode.getServersView();
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

    public PluginsNode getPluginsNode() {
        return pluginsNode;
    }

    public void setPluginsNode(PluginsNode pluginsNode) {
        this.pluginsNode = pluginsNode;
    }
}
