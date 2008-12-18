package org.safehaus.penrose.studio.federation.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.federation.NISRepositoryClient;
import org.safehaus.penrose.federation.*;
import org.safehaus.penrose.studio.federation.nis.editor.NISEditorInput;
import org.safehaus.penrose.studio.federation.nis.editor.NISEditor;
import org.safehaus.penrose.studio.federation.nis.domain.NISDomainNode;
import org.safehaus.penrose.studio.federation.nis.wizard.AddNISDomainWizard;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * @author Endi S. Dewata
 */
public class NISNode extends Node {

    Project project;
    FederationClient federationClient;

    public NISNode(String name, Object parent) throws Exception {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, parent);
    }

    public void init() throws Exception {
        refresh();
    }

    public void refresh() throws Exception {

        log.debug("NIS repositories:");

        children.clear();

        for (FederationRepositoryConfig repositoryConfig : federationClient.getRepositories("NIS")) {

            String repositoryName = repositoryConfig.getName();
            log.debug(" - "+repositoryName);

            NISRepositoryClient nisFederationClient = new NISRepositoryClient(federationClient, repositoryName);

            NISDomainNode domainNode = new NISDomainNode(repositoryConfig.getName(), this);

            domainNode.setProject(project);
            domainNode.setFederationClient(federationClient);
            domainNode.setNisFederationClient(nisFederationClient);
            domainNode.setRepositoryConfig(repositoryConfig);
            domainNode.init();

            children.add(domainNode);
        }
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

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Refresh") {
            public void run() {
                try {
                    refresh();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        NISEditorInput ei = new NISEditorInput();
        ei.setProject(project);
        ei.setFederationClient(federationClient);

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

        federationClient.addRepository(domain);
        federationClient.store();

        federationClient.createPartition(domain.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public FederationClient getFederationClient() {
        return federationClient;
    }

    public void setFederationClient(FederationClient federationClient) {
        this.federationClient = federationClient;
    }
}
