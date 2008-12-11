package org.safehaus.penrose.studio.federation.global;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.federation.FederationDomainNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.federation.FederationClient;

/**
 * @author Endi S. Dewata
 */
public class GlobalNode extends Node {

    Logger log = Logger.getLogger(getClass());

    FederationDomainNode federationDomainNode;

    Project project;
    FederationClient federationClient;

    public GlobalNode(FederationDomainNode federationDomainNode) throws Exception {
        super("Global", PenroseStudio.getImage(PenroseImage.FOLDER), null, federationDomainNode);

        this.federationDomainNode = federationDomainNode;

        project = federationDomainNode.getProject();
        setFederationClient(federationDomainNode.getFederationClient());

        ConflictDetectionNode conflictDetectionNode = new ConflictDetectionNode(
                "Conflict Detection",
                this
        );

        conflictDetectionNode.setProject(project);
        conflictDetectionNode.setFederationClient(federationClient);

        children.add(conflictDetectionNode);
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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