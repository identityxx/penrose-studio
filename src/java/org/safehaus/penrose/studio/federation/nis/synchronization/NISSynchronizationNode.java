package org.safehaus.penrose.studio.federation.nis.synchronization;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Endi S. Dewata
 */
public class NISSynchronizationNode extends Node {

    Project project;
    NISFederationClient nisFederationClient;
    FederationRepositoryConfig repositoryConfig;

    public NISSynchronizationNode(String name, Object parent) {
        super(name, PenroseStudio.getImage(PenroseImage.OBJECT), null, parent);
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

        NISSynchronizationEditorInput ei = new NISSynchronizationEditorInput();
        ei.setProject(project);
        ei.setNisFederationClient(nisFederationClient);
        ei.setDomain(repositoryConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISSynchronizationEditor.class.getName());
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public NISFederationClient getNisFederationClient() {
        return nisFederationClient;
    }

    public void setNisFederationClient(NISFederationClient nisFederationClient) {
        this.nisFederationClient = nisFederationClient;
    }

    public FederationRepositoryConfig getRepositoryConfig() {
        return repositoryConfig;
    }

    public void setRepositoryConfig(FederationRepositoryConfig repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }
}
