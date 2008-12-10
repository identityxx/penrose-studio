package org.safehaus.penrose.studio.federation.global;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;

/**
 * @author Endi S. Dewata
 */
public class ConflictDetectionNode extends Node {

    private Project project;
    private FederationClient federationClient;

    public ConflictDetectionNode(String name, Object parent) {
        super(name, PenroseStudioPlugin.getImage(PenroseImage.FOLDER), null, parent);
    }

    public void open() throws Exception {

        ConflictDetectionInput ei = new ConflictDetectionInput();
        ei.setProject(project);
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}