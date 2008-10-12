package org.safehaus.penrose.studio.federation.nis.linking;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.federation.NISDomain;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.federation.linking.editor.LinkingEditor;
import org.safehaus.penrose.studio.federation.linking.editor.LinkingEditorInput;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.studio.federation.nis.domain.NISDomainNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class NISLinkingNode extends Node {

    private NISNode nisNode;
    private NISDomainNode domainNode;

    private Project project;
    private NISFederationClient nisFederation;

    public NISLinkingNode(String name, NISDomainNode domainNode) {
        super(
                name,
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null,
                domainNode
        );

        this.domainNode = domainNode;
        this.nisNode = domainNode.getNisNode();
        this.project = nisNode.getProject();

        NISNode nisNode = domainNode.getNisNode();
        nisFederation = nisNode.getNisFederation();
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

        FederationRepositoryConfig domain = domainNode.getDomain();
        FederationClient federationClient = nisFederation.getFederationClient();

        LinkingEditorInput ei = new LinkingEditorInput();
        ei.setProject(project);
        ei.setRepository(domain);
        ei.setLocalPartition(domain.getName()+"_"+ NISDomain.NIS);
        ei.setGlobalPartition(federationClient.getName());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, LinkingEditor.class.getName());
    }

    public NISFederationClient getNisFederation() {
        return nisFederation;
    }

    public void setNisTool(NISFederationClient nisFederation) {
        this.nisFederation = nisFederation;
    }
}
