package org.safehaus.penrose.studio.federation.global;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;

/**
 * @author Endi S. Dewata
 */
public class ConflictDetectionNode extends Node {

    private Server server;
    private FederationClient federationClient;

    public ConflictDetectionNode(String name, Node parent) {
        super(name, PenroseStudio.getImage(PenroseImage.OBJECT), null, parent);
    }

    public void open() throws Exception {

        ConflictDetectionInput ei = new ConflictDetectionInput();
        ei.setServer(server);
        ei.setFederationClient(federationClient);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, ConflictDetectionEditor.class.getName());
    }

    public FederationClient getFederationClient() {
        return federationClient;
    }

    public void setFederationClient(FederationClient federationClient) {
        this.federationClient = federationClient;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}