package org.safehaus.penrose.studio.logger;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class LoggingNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;

    public LoggingNode(ServersView view, String name, Object object, Object parent) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), object, parent);
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
                PenroseStudio.getImage(PenroseImage.FOLDER),
                ServersView.APPENDERS,
                null
        ));

        children.add(new LoggersNode(
                view,
                ServersView.LOGGERS,
                PenroseStudio.getImage(PenroseImage.FOLDER),
                ServersView.LOGGERS,
                null
        ));

        return children;
    }
}
