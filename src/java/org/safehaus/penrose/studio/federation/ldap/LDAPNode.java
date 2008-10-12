package org.safehaus.penrose.studio.federation.ldap;

import org.apache.log4j.Logger;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.federation.FederationPartitionNode;
import org.safehaus.penrose.federation.LDAPFederationClient;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.federation.*;
import org.safehaus.penrose.studio.federation.ldap.editor.LDAPEditor;
import org.safehaus.penrose.studio.federation.ldap.editor.LDAPEditorInput;
import org.safehaus.penrose.studio.federation.ldap.repository.LDAPRepositoryNode;
import org.safehaus.penrose.studio.federation.ldap.wizard.AddLDAPRepositoryWizard;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.tree.Node;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class LDAPNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private FederationPartitionNode federationPartitionNode;

    private Project project;
    private LDAPFederationClient ldapFederation;

    //Map<String,Node> children = new TreeMap<String,Node>();

    public LDAPNode(String name, FederationPartitionNode federationPartitionNode) throws Exception {
        super(name, PenroseStudioPlugin.getImage(PenroseImage.FOLDER), null, federationPartitionNode);

        this.federationPartitionNode = federationPartitionNode;
        this.project = federationPartitionNode.getProject();
        this.ldapFederation = new LDAPFederationClient(federationPartitionNode.getFederationClient());
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
    }

    public void open() throws Exception {

        LDAPEditorInput ei = new LDAPEditorInput();
        ei.setProject(project);
        ei.setLdapFederation(ldapFederation);

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

        ldapFederation.addRepository(repository);
        ldapFederation.createPartitions(repository.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        //FederationClient federation = federationPartitionNode.getFederationClient();
        //Collection<FederationRepositoryConfig> children = federation.getRepositories("LDAP");
        return true;
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        FederationClient federation = federationPartitionNode.getFederationClient();
        for (FederationRepositoryConfig repository : federation.getRepositories("LDAP")) {
            LDAPRepositoryNode node = new LDAPRepositoryNode(
                    repository.getName(),
                    repository,
                    this
            );
            children.add(node);
        }

        return children;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public FederationPartitionNode getFederationNode() {
        return federationPartitionNode;
    }

    public void setFederationNode(FederationPartitionNode federationPartitionNode) {
        this.federationPartitionNode = federationPartitionNode;
    }

    public LDAPFederationClient getLdapFederation() {
        return ldapFederation;
    }

    public void setLdapFederation(LDAPFederationClient ldapFederation) {
        this.ldapFederation = ldapFederation;
    }
}
