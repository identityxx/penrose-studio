package org.safehaus.penrose.studio.logging;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.server.ServerNode;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class LoggingNode extends Node {

    Logger log = Logger.getLogger(getClass());

    public LoggingNode(String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        ServerNode serverNode = (ServerNode)getParent();

        children.add(new AppendersNode(
                serverNode.getServer(),
                ObjectsView.APPENDERS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.APPENDERS,
                this
        ));

        children.add(new LoggersNode(
                serverNode.getServer(),
                ObjectsView.LOGGERS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.LOGGERS,
                this
        ));

        return children;
    }
}
