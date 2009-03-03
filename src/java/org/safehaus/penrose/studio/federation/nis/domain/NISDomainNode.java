package org.safehaus.penrose.studio.federation.nis.domain;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.federation.nis.ownership.NISOwnershipNode;
import org.safehaus.penrose.studio.federation.nis.linking.NISLinkingNode;
import org.safehaus.penrose.studio.federation.nis.synchronization.NISSynchronizationNode;
import org.safehaus.penrose.federation.NISRepositoryClient;
import org.safehaus.penrose.studio.federation.nis.wizard.EditNISDomainWizard;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.federation.FederationClient;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Endi S. Dewata
 */
public class NISDomainNode extends Node {

    Server project;
    FederationClient federationClient;
    NISRepositoryClient nisFederationClient;
    FederationRepositoryConfig repositoryConfig;

    public NISDomainNode(String name, Node parent) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, parent);
    }

    public void init() throws Exception {

        NISSynchronizationNode synchronizationNode = new NISSynchronizationNode(
                "Synchronization",
                this
        );

        synchronizationNode.setProject(project);
        synchronizationNode.setNisFederationClient(nisFederationClient);
        synchronizationNode.setRepositoryConfig(repositoryConfig);

        addChild(synchronizationNode);

        NISLinkingNode linkingNode = new NISLinkingNode(
                "Identity Linking",
                this
        );

        linkingNode.setProject(project);
        linkingNode.setNisFederationClient(nisFederationClient);
        linkingNode.setRepositoryConfig(repositoryConfig);

        addChild(linkingNode);

        NISOwnershipNode ownershipNode = new NISOwnershipNode(
                "Ownership Alignment",
                this
        );

        ownershipNode.setProject(project);
        ownershipNode.setNisFederationClient(nisFederationClient);
        ownershipNode.setRepositoryConfig(repositoryConfig);
        ownershipNode.init();

        addChild(ownershipNode);
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

        NISDomainEditorInput ei = new NISDomainEditorInput();
        ei.setProject(project);
        ei.setFederationClient(federationClient);
        ei.setNisFederationClient(nisFederationClient);
        ei.setRepositoryConfig(repositoryConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISDomainEditor.class.getName());
    }

    public void edit() throws Exception {
        
        EditNISDomainWizard wizard = new EditNISDomainWizard(repositoryConfig);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.setPageSize(600, 300);
        int rc = dialog.open();

        if (rc == Window.CANCEL) return;

        federationClient.updateRepository(repositoryConfig);
        federationClient.store();
    }

    public FederationRepositoryConfig getRepositoryConfig() {
        return repositoryConfig;
    }

    public void setRepositoryConfig(FederationRepositoryConfig repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }

    public Server getProject() {
        return project;
    }

    public void setProject(Server project) {
        this.project = project;
    }

    public NISRepositoryClient getNisFederationClient() {
        return nisFederationClient;
    }

    public void setNisFederationClient(NISRepositoryClient nisFederationClient) {
        this.nisFederationClient = nisFederationClient;
    }

    public FederationClient getFederationClient() {
        return federationClient;
    }

    public void setFederationClient(FederationClient federationClient) {
        this.federationClient = federationClient;
    }
}
