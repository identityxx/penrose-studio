package org.safehaus.penrose.studio.federation.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.FederationPartitionNode;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.federation.*;
import org.safehaus.penrose.studio.federation.nis.editor.NISEditorInput;
import org.safehaus.penrose.studio.federation.nis.editor.NISEditor;
import org.safehaus.penrose.studio.federation.nis.ownership.NISOwnershipNode;
import org.safehaus.penrose.studio.federation.nis.linking.NISLinkingNode;
import org.safehaus.penrose.studio.federation.nis.conflict.NISConflictsNode;
import org.safehaus.penrose.studio.federation.nis.domain.NISDomainNode;
import org.safehaus.penrose.studio.federation.nis.wizard.AddNISDomainWizard;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISNode extends Node {

    private FederationPartitionNode federationPartitionNode;

    Project project;
    NISFederationClient nisFederation;

    NISLinkingNode linkingNode;
    NISConflictsNode conflictsNode;
    NISOwnershipNode ownershipNode;

    public NISNode(String name, FederationPartitionNode federationPartitionNode) throws Exception {
        super(name, PenroseStudioPlugin.getImage(PenroseImage.FOLDER), null, federationPartitionNode);

        this.federationPartitionNode = federationPartitionNode;
        this.project = federationPartitionNode.getProject();

        nisFederation = new NISFederationClient(federationPartitionNode.getFederationClient());
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

        manager.add(new Action("New NIS Repository...") {
            public void run() {
                try {
                    addNisDomain();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        NISEditorInput ei = new NISEditorInput();
        ei.setProject(project);
        ei.setNISFederation(nisFederation);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISEditor.class.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void addNisDomain() throws Exception {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        AddNISDomainWizard wizard = new AddNISDomainWizard();
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.setPageSize(600, 300);

        if (dialog.open() == Window.CANCEL) return;

        FederationRepositoryConfig domain = wizard.getRepository();

        nisFederation.addRepository(domain);
        nisFederation.createPartitions(domain.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        //FederationClient federation = federationPartitionNode.getFederationClient();
        //Collection<FederationRepositoryConfig> children = federation.getRepositories("NIS");
        return true;
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        FederationClient federation = federationPartitionNode.getFederationClient();
        for (FederationRepositoryConfig repository : federation.getRepositories("NIS")) {
            NISDomainNode node = new NISDomainNode(
                    repository.getName(),
                    repository,
                    this
            );
            children.add(node);
        }

        return children;
    }

    public NISFederationClient getNisFederation() {
        return nisFederation;
    }

    public void setNisTool(NISFederationClient nisFederation) {
        this.nisFederation = nisFederation;
    }

    public Project getProject() {
        return project;
    }

    public FederationPartitionNode getFederationNode() {
        return federationPartitionNode;
    }

    public void setFederationNode(FederationPartitionNode federationPartitionNode) {
        this.federationPartitionNode = federationPartitionNode;
    }
}
