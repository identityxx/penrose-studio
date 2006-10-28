package org.safehaus.penrose.studio.server;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class ServersNode extends Node {

    Logger log = Logger.getLogger(getClass());

    public ServersNode(
            String name,
            Object object,
            Node parent
    ) {
        super(name, PenrosePlugin.getImage(PenroseImage.FOLDER), object, parent);
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(actions.getPasteAction());
    }

    public boolean canPaste(Object object) throws Exception {
        return object instanceof ServerConfig;
    }

    public void paste(Object object) throws Exception {
        ServerConfig serverConfig = (ServerConfig)object;
        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        int counter = 1;
        String name = serverConfig.getName();

        while (penroseStudio.getServer(name) != null) {
            counter++;
            name = serverConfig.getName()+" ("+counter+")";
        }

        serverConfig.setName(name);
        penroseStudio.addServer(serverConfig);
        penroseStudio.save();
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
                    server.getName(),
                    server,
                    this
            );

            children.add(serverNode);
        }

        return children;
    }
}
