package org.safehaus.penrose.studio.federation.nis.yp;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.studio.federation.nis.domain.NISDomainNode;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Endi S. Dewata
 */
public class NISYPNode extends Node {

    ProjectNode projectNode;
    NISNode nisNode;
    NISDomainNode domainNode;

    private NISFederationClient nisFederation;

    public NISYPNode(String name, NISDomainNode domainNode) {
        super(
                name,
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null,
                domainNode
        );

        this.domainNode = domainNode;

        nisNode = domainNode.getNisNode();
        projectNode = nisNode.getProjectNode();

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

        NISYPEditorInput ei = new NISYPEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setNisFederation(nisFederation);
        ei.setDomain(domainNode.getDomain());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISYPEditor.class.getName());
    }


    public NISFederationClient getNisTool() {
        return nisFederation;
    }

    public void setNisTool(NISFederationClient nisFederation) {
        this.nisFederation = nisFederation;
    }
}
