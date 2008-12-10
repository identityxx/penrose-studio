package org.safehaus.penrose.studio.federation.nis.ownership;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.tree.Node;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class NISFilesNode extends Node {

    private Project project;
    private NISFederationClient nisFederation;
    private FederationRepositoryConfig domain;

    public NISFilesNode(String name, Image image, Object object, Object parent) {
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

    public NISFederationClient getNisTool() {
        return nisFederation;
    }

    public void setNisTool(NISFederationClient nisFederation) {
        this.nisFederation = nisFederation;
    }

    public FederationRepositoryConfig getDomain() {
        return domain;
    }

    public void setDomain(FederationRepositoryConfig domain) {
        this.domain = domain;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
