package org.safehaus.penrose.studio.federation;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.action.RefreshAction;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.module.ModuleManagerClient;
import org.safehaus.penrose.module.ModuleClient;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.federation.Federation;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;

/**
 * @author Endi S. Dewata
 */
public class FederationNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private Server server;

    public FederationNode(String name, Node parent) throws Exception {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, parent);
    }

    public void update() throws Exception {

        PartitionManagerClient partitionManagerClient = server.getClient().getPartitionManagerClient();

        log.debug("Partitions:");

        for (String partitionName : partitionManagerClient.getPartitionNames()) {
            log.debug(" - "+partitionName);

            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

            ModuleManagerClient moduleManagerClient = partitionClient.getModuleManagerClient();
            ModuleClient moduleClient = moduleManagerClient.getModuleClient(Federation.FEDERATION);
            if (!moduleClient.exists()) continue;

            ModuleConfig moduleConfig = moduleClient.getModuleConfig();
            String moduleClass = moduleConfig.getModuleClass();
            //log.debug("   - "+moduleConfig.getName()+" module: "+moduleClass);
            if (!moduleClass.equals("org.safehaus.penrose.federation.module.FederationModule")) continue;

            FederationDomainNode node = new FederationDomainNode(partitionName, this);
            node.setServer(server);
            node.init();

            addChild(node);
        }
    }

    public void expand() throws Exception {
        if (children == null) update();
    }

    public void refresh() throws Exception {
        removeChildren();
        update();
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new RefreshAction(this));
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public boolean hasChildren() {
        return true;
    }
}
