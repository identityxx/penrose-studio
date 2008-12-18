package org.safehaus.penrose.studio.federation.ldap;

import org.apache.log4j.Logger;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.federation.LDAPRepositoryClient;
import org.safehaus.penrose.federation.*;
import org.safehaus.penrose.studio.federation.ldap.editor.LDAPEditor;
import org.safehaus.penrose.studio.federation.ldap.editor.LDAPEditorInput;
import org.safehaus.penrose.studio.federation.ldap.repository.LDAPRepositoryNode;
import org.safehaus.penrose.studio.federation.ldap.wizard.AddLDAPRepositoryWizard;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.tree.Node;

/**
 * @author Endi S. Dewata
 */
public class LDAPNode extends Node {

    Logger log = Logger.getLogger(getClass());

    Project project;
    FederationClient federationClient;

    public LDAPNode(String name, Object parent) throws Exception {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, parent);
    }

    public void init() throws Exception {
        refresh();
    }

    public void refresh() throws Exception {

        log.debug("LDAP repositories:");

        children.clear();

        for (FederationRepositoryConfig repositoryConfig : federationClient.getRepositories("LDAP")) {

            String repositoryName = repositoryConfig.getName();
            log.debug(" - "+repositoryName);
            
            LDAPRepositoryClient ldapFederationClient = new LDAPRepositoryClient(federationClient, repositoryName);

            LDAPRepositoryNode node = new LDAPRepositoryNode(repositoryConfig.getName(), this);

            node.setProject(project);
            node.setFederationClient(federationClient);
            node.setLdapFederationClient(ldapFederationClient);
            node.setRepositoryConfig(repositoryConfig);

            node.init();

            children.add(node);
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

        manager.add(new Action("New LDAP Repository...") {
            public void run() {
                try {
                    addLDAPRepository();
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

        LDAPEditorInput ei = new LDAPEditorInput();
        ei.setProject(project);
        ei.setFederationClient(federationClient);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, LDAPEditor.class.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void addLDAPRepository() throws Exception {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        AddLDAPRepositoryWizard wizard = new AddLDAPRepositoryWizard();
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.setPageSize(600, 300);

        if (dialog.open() == Window.CANCEL) return;

        FederationRepositoryConfig repository = wizard.getRepository();

        federationClient.addRepository(repository);
        federationClient.store();

        federationClient.createPartition(repository.getName());

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
