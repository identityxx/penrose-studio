package org.safehaus.penrose.studio.federation.nis.ownership;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.federation.NISRepositoryClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class NISFilesNode extends Node {

    private Server project;
    private NISRepositoryClient nisFederation;
    private FederationRepositoryConfig domain;

    public NISFilesNode(String name, Image image, Object object, Node parent) {
        super(name, image, object, parent);
    }

    public void open() throws Exception {

        NISFilesEditorInput ei = new NISFilesEditorInput();
        ei.setProject(project);
        ei.setNisTool(nisFederation);
        ei.setDomain(domain);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISFilesEditor.class.getName());
    }

    public NISRepositoryClient getNisTool() {
        return nisFederation;
    }

    public void setNisTool(NISRepositoryClient nisFederation) {
        this.nisFederation = nisFederation;
    }

    public FederationRepositoryConfig getDomain() {
        return domain;
    }

    public void setDomain(FederationRepositoryConfig domain) {
        this.domain = domain;
    }

    public Server getProject() {
        return project;
    }

    public void setProject(Server project) {
        this.project = project;
    }
}
