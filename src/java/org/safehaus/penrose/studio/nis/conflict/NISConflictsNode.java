package org.safehaus.penrose.studio.nis.conflict;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.nis.NISNode;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.studio.nis.domain.NISDomainEditorInput;
import org.safehaus.penrose.studio.nis.domain.NISDomainEditor;
import org.safehaus.penrose.studio.nis.domain.NISDomainNode;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

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

    private NISTool nisTool;

    public NISConflictsNode(String name, String type, Object object, Object parent) {
        super(name, type, PenrosePlugin.getImage(PenroseImage.FOLDER), object, parent);

        domainNode = (NISDomainNode)parent;
        nisNode = domainNode.getNisNode();
        projectNode = nisNode.getProjectNode();
        view = projectNode.getServersView();

        nisTool = nisNode.getNisTool();
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

    public NISTool getNisTool() {
        return nisTool;
    }

    public void setNisTool(NISTool nisTool) {
        this.nisTool = nisTool;
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {
        Collection<Node> children = new ArrayList<Node>();

        NISUsersNode usersNode = new NISUsersNode(
                view,
                "Users",
                "Users",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                null,
                this
        );

        usersNode.setNisTool(nisTool);
        usersNode.setDomain(domainNode.getDomain());

        children.add(usersNode);

        NISGroupsNode groupsNode = new NISGroupsNode(
                view,
                "Groups",
                "Groups",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                null,
                this
        );

        groupsNode.setNisTool(nisTool);
        groupsNode.setDomain(domainNode.getDomain());

        children.add(groupsNode);

        return children;
    }

}
