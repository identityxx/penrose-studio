package org.safehaus.penrose.studio.federation.nis.ownership;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
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
public class NISOwnershipNode extends Node {

    ProjectNode projectNode;
    NISNode nisNode;
    NISDomainNode domainNode;

    private NISFederation nisFederation;

    public NISOwnershipNode(String name, NISDomainNode domainNode) {
        super(
                name,
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null,
                domainNode
        );

        this.domainNode = domainNode;
        
        nisNode = domainNode.getNisNode();
        projectNode = nisNode.getProjectNode();

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

        NISFilesNode filesNode = new NISFilesNode(
                "Files",
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null,
                this
        );

        filesNode.setNisTool(nisFederation);
        filesNode.setDomain(domainNode.getDomain());

        children.add(filesNode);

        return children;
    }

}
