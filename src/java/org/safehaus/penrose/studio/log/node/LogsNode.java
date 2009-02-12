package org.safehaus.penrose.studio.log.node;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.server.node.ServerNode;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.log.node.LoggersNode;
import org.safehaus.penrose.studio.log.node.AppendersNode;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class LogsNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;
    ServerNode serverNode;

    public LogsNode(ServersView view, String name, ServerNode serverNode) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, serverNode);

        this.view = view;
        this.serverNode = serverNode;
    }

    public void init() throws Exception {

        AppendersNode appendersNode = new AppendersNode(
                view,
                ServersView.APPENDERS,
                this
        );
        appendersNode.init();

        children.add(appendersNode);

        LoggersNode loggersNode = new LoggersNode(
                view,
                ServersView.LOGGERS,
                this
        );
        loggersNode.init();

        children.add(loggersNode);
    }

    public ServerNode getServerNode() {
        return serverNode;
    }

    public void setServerNode(ServerNode serverNode) {
        this.serverNode = serverNode;
    }
}
