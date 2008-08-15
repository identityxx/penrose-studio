package org.safehaus.penrose.studio.federation.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.federation.FederationNode;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.federation.*;
import org.safehaus.penrose.studio.federation.nis.editor.NISEditorInput;
import org.safehaus.penrose.studio.federation.nis.editor.NISEditor;
import org.safehaus.penrose.studio.federation.nis.ownership.NISOwnershipNode;
import org.safehaus.penrose.studio.federation.nis.linking.NISLinkingNode;
import org.safehaus.penrose.studio.federation.nis.conflict.NISConflictsNode;
import org.safehaus.penrose.studio.federation.nis.domain.NISDomainNode;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISNode extends Node {

    private ProjectNode projectNode;
    private FederationNode federationNode;

    NISFederationClient nisFederation;

    NISLinkingNode linkingNode;
    NISConflictsNode conflictsNode;
    NISOwnershipNode ownershipNode;

    public NISNode(String name, FederationNode federationNode) throws Exception {
        super(name, PenroseStudioPlugin.getImage(PenroseImage.FOLDER), null, federationNode);

        this.federationNode = federationNode;
        this.projectNode = federationNode.getProjectNode();

        nisFederation = new NISFederationClient(federationNode.getFederation());
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

        NISEditorInput ei = new NISEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setNisTool(nisFederation);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISEditor.class.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        FederationClient federation = federationNode.getFederation();
        Collection<Repository> children = federation.getRepositories("NIS");
        return !children.isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        FederationClient federation = federationNode.getFederation();
        for (Repository repository : federation.getRepositories("NIS")) {
            NISDomainNode node = new NISDomainNode(
                    repository.getName(),
                    (NISDomain)repository,
                    this
            );
            children.add(node);
        }

        return children;
    }

    public NISFederationClient getNisFederation() {
        return nisFederation;
    }

    public void setNisTool(NISFederationClient nisFederation) {
        this.nisFederation = nisFederation;
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }

    public FederationNode getFederationNode() {
        return federationNode;
    }

    public void setFederationNode(FederationNode federationNode) {
        this.federationNode = federationNode;
    }
}
