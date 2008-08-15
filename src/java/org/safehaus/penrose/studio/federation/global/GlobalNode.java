package org.safehaus.penrose.studio.federation.global;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.federation.FederationNode;
import org.safehaus.penrose.studio.federation.global.editor.GlobalEditor;
import org.safehaus.penrose.studio.federation.global.editor.GlobalEditorInput;
import org.safehaus.penrose.studio.federation.global.wizard.GlobalRepositoryEditorWizard;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class GlobalNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ProjectNode projectNode;
    private FederationNode federationNode;

    public GlobalNode(String name, FederationNode federationNode) {
        super(name, PenroseStudioPlugin.getImage(PenroseImage.FOLDER), null, federationNode);

        this.federationNode = federationNode;
        this.projectNode = federationNode.getProjectNode();
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

        GlobalEditorInput ei = new GlobalEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setFederation(federationNode.getFederation());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, GlobalEditor.class.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void edit() throws Exception {

        FederationClient federation = federationNode.getFederation();
        GlobalRepositoryEditorWizard wizard = new GlobalRepositoryEditorWizard(federation.getGlobalRepository());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.setPageSize(600, 300);

        if (dialog.open() == Window.CANCEL) return;

        federation.updateGlobalRepository(wizard.getRepository());
    }
    
    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }

    public FederationNode getFederationNode() {
        return federationNode;
    }

    public void setFederationNode(FederationNode federationNode) {
        this.federationNode = federationNode;
    }
}
