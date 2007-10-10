package org.safehaus.penrose.studio.plugin;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.federation.FederationNode;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class PluginsNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ServersView view;
    private ProjectNode projectNode;

    protected Collection<Node> children = new ArrayList<Node>();

    public PluginsNode(String name, String type, Object object, Object parent) throws Exception {
        super(name, type, PenrosePlugin.getImage(PenroseImage.FOLDER), object, parent);

        projectNode = (ProjectNode)parent;
        view = projectNode.getServersView();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        if (penroseStudio.getLicense() != null) {

            FederationNode pluginNode = new FederationNode(this);
            children.add(pluginNode);
        }
    }

    public boolean hasChildren() throws Exception {
        return !children.isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {
        return children;
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
}
