package org.safehaus.penrose.studio.federation.ldap.repository;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.federation.ldap.linking.LDAPLinkingNode;
import org.safehaus.penrose.federation.LDAPRepositoryClient;
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

    private Server server;
    private FederationClient federationClient;
    private LDAPRepositoryClient ldapFederationClient;
    private FederationRepositoryConfig repositoryConfig;

    public LDAPRepositoryNode(String name, Node parent) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, parent );
    }

    public void init() throws Exception {

        LDAPLinkingNode linkingNode = new LDAPLinkingNode(
                "Identity Linking",
                this
        );

        linkingNode.setProject(server);
        linkingNode.setLdapFederationClient(ldapFederationClient);
        linkingNode.setRepositoryConfig(repositoryConfig);

        addChild(linkingNode);
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
        ei.setProject(server);
        ei.setFederationClient(federationClient);
        ei.setLdapFederationClient(ldapFederationClient);
        ei.setRepositoryConfig(repositoryConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, LDAPRepositoryEditor.class.getName());
    }

    public void edit() throws Exception {

        EditLDAPRepositoryWizard wizard = new EditLDAPRepositoryWizard(repositoryConfig);
        wizard.setServer(server);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.setPageSize(600, 300);
        int rc = dialog.open();

        if (rc == Window.CANCEL) return;

        federationClient.updateRepository(repositoryConfig);
        federationClient.store();
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public FederationRepositoryConfig getRepositoryConfig() {
        return repositoryConfig;
    }

    public void setRepositoryConfig(FederationRepositoryConfig repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }

    public LDAPRepositoryClient getLdapFederationClient() {
        return ldapFederationClient;
    }

    public void setLdapFederationClient(LDAPRepositoryClient ldapFederationClient) {
        this.ldapFederationClient = ldapFederationClient;
    }

    public FederationClient getFederationClient() {
        return federationClient;
    }

    public void setFederationClient(FederationClient federationClient) {
        this.federationClient = federationClient;
    }
}
