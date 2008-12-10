package org.safehaus.penrose.studio.federation.nis.ownership;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.federation.nis.ownership.OwnershipAlignmentEditor;
import org.safehaus.penrose.studio.federation.nis.ownership.OwnershipAlignmentInput;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class NISUsersNode extends Node {

    private Project project;
    private NISFederationClient nisFederationClient;
    private FederationRepositoryConfig repositoryConfig;

    public NISUsersNode(String name, Object parent) {
        super(name, PenroseStudioPlugin.getImage(PenroseImage.FOLDER), null, parent);
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

    public NISFederationClient getNisFederationClient() {
        return nisFederationClient;
    }

    public void setNisFederationClient(NISFederationClient nisFederation) {
        this.nisFederationClient = nisFederation;
    }

    public FederationRepositoryConfig getRepositoryConfig() {
        return repositoryConfig;
    }

    public void setRepositoryConfig(FederationRepositoryConfig repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
