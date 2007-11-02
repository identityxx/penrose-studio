package org.safehaus.penrose.studio.federation.ldap.linking;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.federation.ldap.repository.LDAPRepositoryNode;
import org.safehaus.penrose.studio.federation.ldap.LDAPNode;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.studio.federation.linking.LinkingEditorInput;
import org.safehaus.penrose.studio.federation.linking.LinkingEditor;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.server.ServersView;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Endi S. Dewata
 */
public class LDAPLinkingNode extends Node {

    ProjectNode projectNode;
    LDAPNode ldapNode;
    LDAPRepositoryNode repositoryNode;

    private LDAPFederation ldapFederation;

    public LDAPLinkingNode(String name, LDAPRepositoryNode repositoryNode) {
        super(
                name,
                ServersView.ENTRY,
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null, 
                repositoryNode
        );

        this.repositoryNode = repositoryNode;
        this.ldapNode = repositoryNode.getLdapNode();
        this.projectNode = ldapNode.getProjectNode();

        ldapFederation = ldapNode.getLdapFederation();
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

        LinkingEditorInput ei = new LinkingEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setRepository(repositoryNode.getRepository());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, LinkingEditor.class.getName());
    }

    public LDAPFederation getLdapFederation() {
        return ldapFederation;
    }

    public void setLdapFederation(LDAPFederation ldapFederation) {
        this.ldapFederation = ldapFederation;
    }
}
