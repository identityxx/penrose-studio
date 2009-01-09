package org.safehaus.penrose.studio.federation.ldap.linking;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.federation.LDAPRepositoryClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.federation.linking.editor.IdentityLinkingEditor;
import org.safehaus.penrose.studio.federation.linking.editor.IdentityLinkingEditorInput;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class LDAPLinkingNode extends Node {

    Server project;
    LDAPRepositoryClient ldapFederationClient;
    FederationRepositoryConfig repositoryConfig;

    public LDAPLinkingNode(String name, Node parent) {
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

        FederationClient federationClient = ldapFederationClient.getFederationClient();
        
        IdentityLinkingEditorInput ei = new IdentityLinkingEditorInput();
        ei.setProject(project);
        ei.setRepository(repositoryConfig);
        ei.setSourcePartition(federationClient.getFederationDomain()+"_"+ repositoryConfig.getName());
        ei.setTargetPartition(federationClient.getFederationDomain());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, IdentityLinkingEditor.class.getName());
    }

    public LDAPRepositoryClient getLdapFederationClient() {
        return ldapFederationClient;
    }

    public void setLdapFederationClient(LDAPRepositoryClient ldapFederationClient) {
        this.ldapFederationClient = ldapFederationClient;
    }

    public Server getProject() {
        return project;
    }

    public void setProject(Server project) {
        this.project = project;
    }

    public FederationRepositoryConfig getRepositoryConfig() {
        return repositoryConfig;
    }

    public void setRepositoryConfig(FederationRepositoryConfig repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }
}
