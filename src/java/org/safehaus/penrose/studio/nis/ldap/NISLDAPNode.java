package org.safehaus.penrose.studio.nis.ldap;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.nis.NISNode;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.studio.nis.domain.NISDomainNode;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Endi S. Dewata
 */
public class NISLDAPNode extends Node {

    ServersView view;
    ProjectNode projectNode;
    NISNode nisNode;
    NISDomainNode domainNode;

    private NISTool nisTool;

    public NISLDAPNode(String name, String type, Object object, Object parent) {
        super(name, type, PenrosePlugin.getImage(PenroseImage.FOLDER), object, parent);

        domainNode = (NISDomainNode)parent;
        nisNode = domainNode.getNisNode();
        projectNode = nisNode.getProjectNode();
        view = projectNode.getServersView();

        nisTool = nisNode.getNisTool();
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

        NISLDAPEditorInput ei = new NISLDAPEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setNisTool(nisTool);
        ei.setDomain(domainNode.getDomain());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISLDAPEditor.class.getName());
    }


    public NISTool getNisTool() {
        return nisTool;
    }

    public void setNisTool(NISTool nisTool) {
        this.nisTool = nisTool;
    }
}
