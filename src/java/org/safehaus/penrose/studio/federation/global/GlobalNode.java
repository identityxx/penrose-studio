package org.safehaus.penrose.studio.federation.global;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.federation.FederationDomainNode;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.federation.FederationClient;

/**
 * @author Endi S. Dewata
 */
public class GlobalNode extends Node {

    Logger log = Logger.getLogger(getClass());

    FederationDomainNode federationDomainNode;

    Server server;
    FederationClient federationClient;

    public GlobalNode(FederationDomainNode federationDomainNode) throws Exception {
        super("Global", PenroseStudio.getImage(PenroseImage.FOLDER), null, federationDomainNode);

        this.federationDomainNode = federationDomainNode;

        server = federationDomainNode.getServer();
        federationClient = federationDomainNode.getFederationClient();

        ConflictDetectionNode conflictDetectionNode = new ConflictDetectionNode(
                "Conflict Detection",
                this
        );

        conflictDetectionNode.setServer(server);
        conflictDetectionNode.setFederationClient(federationClient);

        addChild(conflictDetectionNode);
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });
    }

    public void open() throws Exception {
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public FederationDomainNode getFederationNode() {
        return federationDomainNode;
    }

    public void setFederationNode(FederationDomainNode federationDomainNode) {
        this.federationDomainNode = federationDomainNode;
    }

    public FederationClient getFederationClient() {
        return federationClient;
    }

    public void setFederationClient(FederationClient federationClient) {
        this.federationClient = federationClient;
    }
}