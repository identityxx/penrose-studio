package org.safehaus.penrose.studio.logging;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class LoggingNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;

    public LoggingNode(ServersView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        children.add(new AppendersNode(
                view,
                ServersView.APPENDERS,
                ServersView.APPENDERS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ServersView.APPENDERS,
                null
        ));

        children.add(new LoggersNode(
                view,
                ServersView.LOGGERS,
                ServersView.LOGGERS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ServersView.LOGGERS,
                null
        ));

        return children;
    }
}
