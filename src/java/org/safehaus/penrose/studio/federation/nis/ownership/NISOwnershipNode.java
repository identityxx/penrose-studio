package org.safehaus.penrose.studio.federation.nis.ownership;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.federation.NISRepositoryClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Endi S. Dewata
 */
public class NISOwnershipNode extends Node {

    Server server;
    NISRepositoryClient nisFederationClient;
    FederationRepositoryConfig repositoryConfig;

    public NISOwnershipNode(String name, Node parent) {
        super(name, PenroseStudio.getImage(PenroseImage.OBJECT), null, parent);
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

        OwnershipAlignmentInput ei = new OwnershipAlignmentInput();
        ei.setServer(server);
        ei.setNisFederationClient(nisFederationClient);
        ei.setDomain(repositoryConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, OwnershipAlignmentEditor.class.getName());
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public NISRepositoryClient getNisFederationClient() {
        return nisFederationClient;
    }

    public void setNisFederationClient(NISRepositoryClient nisFederationClient) {
        this.nisFederationClient = nisFederationClient;
    }

    public FederationRepositoryConfig getRepositoryConfig() {
        return repositoryConfig;
    }

    public void setRepositoryConfig(FederationRepositoryConfig repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }
}
