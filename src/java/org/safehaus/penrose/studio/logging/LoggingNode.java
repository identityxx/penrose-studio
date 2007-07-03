package org.safehaus.penrose.studio.logging;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
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

    ObjectsView view;

    public LoggingNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        children.add(new AppendersNode(
                view,
                ObjectsView.APPENDERS,
                ObjectsView.APPENDERS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.APPENDERS,
                null
        ));

        children.add(new LoggersNode(
                view,
                ObjectsView.LOGGERS,
                ObjectsView.LOGGERS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.LOGGERS,
                null
        ));

        return children;
    }
}
