package org.safehaus.penrose.studio.federation.nis.linking;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.federation.linking.editor.IdentityLinkingEditor;
import org.safehaus.penrose.studio.federation.linking.editor.IdentityLinkingEditorInput;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.studio.federation.nis.domain.NISDomainNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class NISLinkingNode extends Node {

    Project project;
    NISFederationClient nisFederationClient;
    FederationRepositoryConfig repositoryConfig;

    public NISLinkingNode(String name, NISDomainNode domainNode) {
        super(
                name,
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null,
                domainNode
        );

        repositoryConfig = domainNode.getDomain();
        project = domainNode.getProject();
        nisFederationClient = domainNode.getNisFederationClient();
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

        FederationClient federationClient = nisFederationClient.getFederationClient();

        IdentityLinkingEditorInput ei = new IdentityLinkingEditorInput();
        ei.setProject(project);
        ei.setRepository(repositoryConfig);
        ei.setSourcePartition(federationClient.getName()+"_"+ repositoryConfig.getName());
        ei.setTargetPartition(federationClient.getName());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, IdentityLinkingEditor.class.getName());
    }

    public NISFederationClient getNisFederationClient() {
        return nisFederationClient;
    }

    public void setNisFederationClient(NISFederationClient nisFederation) {
        this.nisFederationClient = nisFederation;
    }
}
