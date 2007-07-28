package org.safehaus.penrose.studio.handler;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.handler.HandlerConfig;
import org.eclipse.swt.graphics.Image;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class HandlersNode extends Node {

    ObjectsView view;

    public HandlersNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        Collection handlerConfigs = penroseApplication.getPenroseConfig().getHandlerConfigs();

        for (Iterator i=handlerConfigs.iterator(); i.hasNext(); ) {
            HandlerConfig handlerConfig = (HandlerConfig)i.next();

            children.add(new HandlerNode(
                    view,
                    handlerConfig.getName(),
                    ObjectsView.HANDLER,
                    PenrosePlugin.getImage(PenroseImage.HANDLER),
                    handlerConfig,
                    this
            ));
        }

        return children;
    }
}
