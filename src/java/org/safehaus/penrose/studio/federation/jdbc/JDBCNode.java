package org.safehaus.penrose.studio.federation.jdbc;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.federation.FederationDomainNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class JDBCNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private FederationDomainNode federationDomainNode;
    private Project project;

    public JDBCNode(String name, FederationDomainNode federationDomainNode) throws Exception {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, federationDomainNode);

        this.federationDomainNode = federationDomainNode;
        this.project = federationDomainNode.getProject();
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
}