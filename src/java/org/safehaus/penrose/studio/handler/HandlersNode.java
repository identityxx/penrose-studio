package org.safehaus.penrose.studio.handler;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.handler.HandlerConfig;
import org.eclipse.swt.graphics.Image;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class HandlersNode extends Node {

    ServersView view;
    ProjectNode projectNode;

    public HandlersNode(ServersView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        projectNode = (ProjectNode)parent;
        this.view = projectNode.getView();
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        Project project = projectNode.getProject();
        Collection handlerConfigs = project.getPenroseConfig().getHandlerConfigs();

        for (Iterator i=handlerConfigs.iterator(); i.hasNext(); ) {
            HandlerConfig handlerConfig = (HandlerConfig)i.next();

            children.add(new HandlerNode(
                    view,
                    handlerConfig.getName(),
                    ServersView.HANDLER,
                    PenrosePlugin.getImage(PenroseImage.HANDLER),
                    handlerConfig,
                    this
            ));
        }

        return children;
    }
}
