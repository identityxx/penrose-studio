package org.safehaus.penrose.studio.federation.nis.conflict;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.studio.federation.nis.domain.NISDomainNode;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISConflictsNode extends Node {

    NISNode nisNode;
    NISDomainNode domainNode;

    private Project project;
    private NISFederationClient nisFederationClient;

    public NISConflictsNode(String name, NISDomainNode domainNode) {
        super(
                name,
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null, 
                domainNode
        );

        this.domainNode = domainNode;

        nisNode = domainNode.getNisNode();

        project = nisNode.getProject();
        nisFederationClient = nisNode.getNisFederation();
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

    public NISFederationClient getNisTool() {
        return nisFederationClient;
    }

    public void setNisTool(NISFederationClient nisFederation) {
        this.nisFederationClient = nisFederation;
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {
        Collection<Node> children = new ArrayList<Node>();

        NISUsersNode usersNode = new NISUsersNode(
                "Users",
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null,
                this
        );

        usersNode.setProject(project);
        usersNode.setNisFederationClient(nisFederationClient);
        usersNode.setDomain(domainNode.getDomain());

        children.add(usersNode);

        NISGroupsNode groupsNode = new NISGroupsNode(
                "Groups",
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null,
                this
        );

        groupsNode.setProject(project);
        groupsNode.setNisTool(nisFederationClient);
        groupsNode.setDomain(domainNode.getDomain());

        children.add(groupsNode);

        return children;
    }

}
