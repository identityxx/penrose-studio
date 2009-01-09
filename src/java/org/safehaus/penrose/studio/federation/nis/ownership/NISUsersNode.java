package org.safehaus.penrose.studio.federation.nis.ownership;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.safehaus.penrose.federation.NISRepositoryClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.federation.nis.ownership.OwnershipAlignmentEditor;
import org.safehaus.penrose.studio.federation.nis.ownership.OwnershipAlignmentInput;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;

/**
 * @author Endi S. Dewata
 */
public class NISUsersNode extends Node {

    private Server project;
    private NISRepositoryClient nisFederationClient;
    private FederationRepositoryConfig repositoryConfig;

    public NISUsersNode(String name, Node parent) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, parent);
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

        OwnershipAlignmentInput ei = new OwnershipAlignmentInput();
        ei.setProject(project);
        ei.setNisFederationClient(nisFederationClient);
        ei.setDomain(repositoryConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, OwnershipAlignmentEditor.class.getName());
    }

    public NISRepositoryClient getNisFederationClient() {
        return nisFederationClient;
    }

    public void setNisFederationClient(NISRepositoryClient nisFederation) {
        this.nisFederationClient = nisFederation;
    }

    public FederationRepositoryConfig getRepositoryConfig() {
        return repositoryConfig;
    }

    public void setRepositoryConfig(FederationRepositoryConfig repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }

    public Server getProject() {
        return project;
    }

    public void setProject(Server project) {
        this.project = project;
    }
}
