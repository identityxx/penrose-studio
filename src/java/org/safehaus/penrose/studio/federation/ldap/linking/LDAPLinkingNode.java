package org.safehaus.penrose.studio.federation.ldap.linking;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.federation.repository.LDAPRepository;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.studio.federation.ldap.LDAPNode;
import org.safehaus.penrose.studio.federation.ldap.repository.LDAPRepositoryNode;
import org.safehaus.penrose.studio.federation.linking.editor.LinkingEditor;
import org.safehaus.penrose.studio.federation.linking.editor.LinkingEditorInput;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.tree.Node;

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

        LDAPRepository repository = repositoryNode.getRepository();
        
        LinkingEditorInput ei = new LinkingEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setRepository(repository);
        ei.setPartitionName(repository.getName());

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
