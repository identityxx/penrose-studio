package org.safehaus.penrose.studio.federation.jdbc;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;

/**
 * @author Endi S. Dewata
 */
public class JDBCNode extends Node {

    Logger log = Logger.getLogger(getClass());

    Server project;
    FederationClient federationClient;

    public JDBCNode(String name, Node parent) throws Exception {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, parent);
    }

    public void init() throws Exception {

        log.debug("JDBC repositories:");

        removeChildren();

        for (FederationRepositoryConfig repositoryConfig : federationClient.getRepositories("JDBC")) {

            log.debug(" - "+repositoryConfig.getName());
        }
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {
    }

    public Server getProject() {
        return project;
    }

    public void setProject(Server project) {
        this.project = project;
    }

    public FederationClient getFederationClient() {
        return federationClient;
    }

    public void setFederationClient(FederationClient federationClient) {
        this.federationClient = federationClient;
    }
}