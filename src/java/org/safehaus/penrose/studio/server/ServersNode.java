package org.safehaus.penrose.studio.server;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.util.PenroseStudioClipboard;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class ServersNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    public ServersNode(
            ObjectsView view,
            String name,
            String type,
            Object object,
            Node parent
    ) {
        super(name, type, PenrosePlugin.getImage(PenroseImage.FOLDER), object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(actions.getPasteAction());
    }

    public void paste(PenroseStudioClipboard clipboard) throws Exception {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Object object = clipboard.get();
        log.debug("Pasting: "+object);
        
        if (object instanceof ServerConfig) {
            ServerConfig serverConfig = (ServerConfig)object;
            serverConfig.setName(serverConfig.getName()+" (2)");
            penroseStudio.addServer(serverConfig);
        }
    }

    public boolean hasChildren() throws Exception {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        return !penroseStudio.getServers().isEmpty();
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        for (Iterator i=penroseStudio.getServers().iterator(); i.hasNext(); ) {
            Server server = (Server)i.next();

            ServerNode serverNode = new ServerNode(
                    view,
                    server.getName(),
                    ObjectsView.PROJECT,
                    server,
                    this
            );

            children.add(serverNode);
        }

        return children;
    }
}
