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
import org.safehaus.penrose.studio.federation.linking.editor.IdentityLinkingEditor;
import org.safehaus.penrose.studio.federation.linking.editor.IdentityLinkingEditorInput;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class LDAPLinkingNode extends Node {

    Project project;
    LDAPFederationClient ldapFederationClient;
    FederationRepositoryConfig repositoryConfig;

    public LDAPLinkingNode(String name, Object parent) {
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
        ei.setSourcePartition(federationClient.getName()+"_"+ repositoryConfig.getName());
        ei.setTargetPartition(federationClient.getName());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, IdentityLinkingEditor.class.getName());
    }

    public LDAPFederationClient getLdapFederationClient() {
        return ldapFederationClient;
    }

    public void setLdapFederationClient(LDAPFederationClient ldapFederationClient) {
        this.ldapFederationClient = ldapFederationClient;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public FederationRepositoryConfig getRepositoryConfig() {
        return repositoryConfig;
    }

    public void setRepositoryConfig(FederationRepositoryConfig repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }
}
