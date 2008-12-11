package org.safehaus.penrose.studio.federation.ldap.linking;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.federation.LDAPFederationClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.federation.ldap.LDAPNode;
import org.safehaus.penrose.studio.federation.ldap.repository.LDAPRepositoryNode;
import org.safehaus.penrose.studio.federation.linking.editor.IdentityLinkingEditor;
import org.safehaus.penrose.studio.federation.linking.editor.IdentityLinkingEditorInput;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class LDAPLinkingNode extends Node {

    Project project;
    LDAPNode ldapNode;
    LDAPRepositoryNode repositoryNode;

    private LDAPFederationClient ldapFederation;

    public LDAPLinkingNode(String name, LDAPRepositoryNode repositoryNode) {
        super(name, PenroseStudio.getImage(PenroseImage.OBJECT), null, repositoryNode);

        this.repositoryNode = repositoryNode;
        this.ldapNode = repositoryNode.getLdapNode();
        this.project = ldapNode.getProject();

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

        FederationRepositoryConfig repository = repositoryNode.getRepository();
        FederationClient federationClient = ldapFederation.getFederationClient();
        
        IdentityLinkingEditorInput ei = new IdentityLinkingEditorInput();
        ei.setProject(project);
        ei.setRepository(repository);
        ei.setSourcePartition(federationClient.getName()+"_"+repository.getName());
        ei.setTargetPartition(federationClient.getName());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, IdentityLinkingEditor.class.getName());
    }

    public LDAPFederationClient getLdapFederation() {
        return ldapFederation;
    }

    public void setLdapFederation(LDAPFederationClient ldapFederation) {
        this.ldapFederation = ldapFederation;
    }
}
