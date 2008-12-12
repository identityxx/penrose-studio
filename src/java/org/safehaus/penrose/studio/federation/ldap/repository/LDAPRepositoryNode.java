package org.safehaus.penrose.studio.federation.ldap.repository;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.federation.ldap.linking.LDAPLinkingNode;
import org.safehaus.penrose.federation.LDAPFederationClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.federation.ldap.wizard.EditLDAPRepositoryWizard;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * @author Endi S. Dewata
 */
public class LDAPRepositoryNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private FederationClient federationClient;
    private LDAPFederationClient ldapFederationClient;
    private FederationRepositoryConfig repositoryConfig;

    public LDAPRepositoryNode(String name, Object parent) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, parent );
    }

    public void init() throws Exception {

        LDAPLinkingNode linkingNode = new LDAPLinkingNode(
                "Identity Linking",
                this
        );

        linkingNode.setProject(project);
        linkingNode.setLdapFederationClient(ldapFederationClient);
        linkingNode.setRepositoryConfig(repositoryConfig);

        children.add(linkingNode);
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

        manager.add(new Action("Edit") {
            public void run() {
                try {
                    edit();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        LDAPRepositoryEditorInput ei = new LDAPRepositoryEditorInput();
        ei.setProject(project);
        ei.setFederationClient(federationClient);
        ei.setLdapFederationClient(ldapFederationClient);
        ei.setRepositoryConfig(repositoryConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, LDAPRepositoryEditor.class.getName());
    }

    public void edit() throws Exception {

        EditLDAPRepositoryWizard wizard = new EditLDAPRepositoryWizard(repositoryConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.setPageSize(600, 300);

        if (dialog.open() == Window.CANCEL) return;

        federationClient.updateRepository(repositoryConfig);
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

    public LDAPFederationClient getLdapFederationClient() {
        return ldapFederationClient;
    }

    public void setLdapFederationClient(LDAPFederationClient ldapFederationClient) {
        this.ldapFederationClient = ldapFederationClient;
    }

    public FederationClient getFederationClient() {
        return federationClient;
    }

    public void setFederationClient(FederationClient federationClient) {
        this.federationClient = federationClient;
    }
}
