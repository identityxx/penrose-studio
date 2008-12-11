package org.safehaus.penrose.studio.federation.nis.domain;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.federation.nis.ownership.NISOwnershipNode;
import org.safehaus.penrose.studio.federation.nis.linking.NISLinkingNode;
import org.safehaus.penrose.studio.federation.nis.synchronization.NISSynchronizationNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.studio.federation.nis.wizard.EditNISDomainWizard;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
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

    private NISNode nisNode;

    Project project;
    NISFederationClient nisFederationClient;
    FederationRepositoryConfig domain;

    NISSynchronizationNode synchronizationNode;
    NISLinkingNode   linkingNode;
    NISOwnershipNode ownershipNode;

    public NISDomainNode(String name, FederationRepositoryConfig domain, NISNode nisNode) {
        super(
                name,
                PenroseStudio.getImage(PenroseImage.FOLDER),
                domain,
                nisNode
        );

        this.domain = domain;
        this.nisNode = nisNode;

        nisFederationClient = nisNode.getNisFederation();
        project = nisNode.getProject();

        synchronizationNode = new NISSynchronizationNode(
                "Synchronization",
                this
        );

        children.add(synchronizationNode);

        linkingNode = new NISLinkingNode(
                "Identity Linking",
                this
        );

        children.add(linkingNode);

        ownershipNode = new NISOwnershipNode(
                "Ownership Alignment",
                this
        );

        ownershipNode.setProject(project);
        ownershipNode.setNisFederationClient(nisFederationClient);
        ownershipNode.setRepositoryConfig(domain);
        ownershipNode.init();

        children.add(ownershipNode);

/*
        consolidationNode = new NISConsolidationNode(
                "Stacking Authentication",
                this
        );

        children.add(consolidationNode);
*/
    }

    public Image getImage() {
        return PenroseStudio.getImage(PenroseImage.FOLDER);
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
        ei.setNisFederation(nisFederationClient);
        ei.setDomain(domain);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISDomainEditor.class.getName());
    }

    public void edit() throws Exception {
        
        EditNISDomainWizard wizard = new EditNISDomainWizard(domain);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.setPageSize(600, 300);

        if (dialog.open() == Window.CANCEL) return;

        nisFederationClient.updateRepository(domain);
    }

    public FederationRepositoryConfig getDomain() {
        return domain;
    }

    public void setDomain(FederationRepositoryConfig domain) {
        this.domain = domain;
    }

    public NISFederationClient getNisTool() {
        return nisFederationClient;
    }

    public void setNisTool(NISFederationClient nisFederation) {
        this.nisFederationClient = nisFederation;
    }

    public NISNode getNisNode() {
        return nisNode;
    }

    public void setNisNode(NISNode nisNode) {
        this.nisNode = nisNode;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public NISFederationClient getNisFederationClient() {
        return nisFederationClient;
    }

    public void setNisFederationClient(NISFederationClient nisFederationClient) {
        this.nisFederationClient = nisFederationClient;
    }
}
