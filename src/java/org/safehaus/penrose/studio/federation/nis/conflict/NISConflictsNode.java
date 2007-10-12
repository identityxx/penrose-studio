package org.safehaus.penrose.studio.federation.nis.conflict;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.NISDomainNode;
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

    ServersView view;
    ProjectNode projectNode;
    NISNode nisNode;
    NISDomainNode domainNode;

    private NISFederation nisFederation;

    public NISConflictsNode(String name, NISDomainNode domainNode) {
        super(
                name,
                ServersView.ENTRY,
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null, 
                domainNode
        );

        this.domainNode = domainNode;

        nisNode = domainNode.getNisNode();
        projectNode = nisNode.getProjectNode();
        view = projectNode.getServersView();

        nisFederation = nisNode.getNisFederation();
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

    public NISFederation getNisTool() {
        return nisFederation;
    }

    public void setNisTool(NISFederation nisFederation) {
        this.nisFederation = nisFederation;
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {
        Collection<Node> children = new ArrayList<Node>();

        NISUsersNode usersNode = new NISUsersNode(
                "Users",
                "Users",
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null,
                this
        );

        usersNode.setNisTool(nisFederation);
        usersNode.setDomain(domainNode.getDomain());

        children.add(usersNode);

        NISGroupsNode groupsNode = new NISGroupsNode(
                "Groups",
                "Groups",
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null,
                this
        );

        groupsNode.setNisTool(nisFederation);
        groupsNode.setDomain(domainNode.getDomain());

        children.add(groupsNode);

        return children;
    }

}
