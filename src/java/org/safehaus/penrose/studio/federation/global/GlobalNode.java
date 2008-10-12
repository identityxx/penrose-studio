package org.safehaus.penrose.studio.federation.global;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.federation.FederationPartitionNode;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class GlobalNode extends Node {

    Logger log = Logger.getLogger(getClass());

    public GlobalNode(String name, FederationPartitionNode federationPartitionNode) {
        super(name, PenroseStudioPlugin.getImage(PenroseImage.FOLDER), null, federationPartitionNode);
    }

    public void showMenu(IMenuManager manager) throws Exception {
    }
}