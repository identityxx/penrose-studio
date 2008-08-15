package org.safehaus.penrose.studio.federation.ldap.repository;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.federation.ldap.linking.LDAPLinkingNode;
import org.safehaus.penrose.studio.federation.ldap.LDAPNode;
import org.safehaus.penrose.federation.LDAPRepository;
import org.safehaus.penrose.federation.LDAPFederationClient;
import org.safehaus.penrose.studio.federation.ldap.wizard.EditLDAPRepositoryWizard;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class LDAPRepositoryNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ProjectNode projectNode;
    private LDAPNode ldapNode;

    private LDAPFederationClient ldapFederation;
    private LDAPRepository repository;

    Collection<Node> children = new ArrayList<Node>();

    LDAPLinkingNode   linkingNode;

    public LDAPRepositoryNode(String name, LDAPRepository repository, LDAPNode ldapNode) {
        super(
                name,
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                repository,
                ldapNode
        );

        this.repository = repository;
        this.ldapNode = ldapNode;
        this.ldapFederation = ldapNode.getLdapFederation();

        projectNode = ldapNode.getProjectNode();

        linkingNode = new LDAPLinkingNode(
                "Identity Linking",
                this
        );

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
        ei.setProject(projectNode.getProject());
        ei.setLdapFederation(ldapFederation);
        ei.setRepository(repository);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, LDAPRepositoryEditor.class.getName());
    }

    public void edit() throws Exception {

        EditLDAPRepositoryWizard wizard = new EditLDAPRepositoryWizard(repository);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.setPageSize(600, 300);

        if (dialog.open() == Window.CANCEL) return;

        ldapFederation.updateRepository(repository);
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }

    public LDAPNode getLdapNode() {
        return ldapNode;
    }

    public void setLdapNode(LDAPNode ldapNode) {
        this.ldapNode = ldapNode;
    }

    public LDAPRepository getRepository() {
        return repository;
    }

    public void setRepository(LDAPRepository repository) {
        this.repository = repository;
    }

    public boolean hasChildren() throws Exception {
        return !children.isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {
        return children;
    }
}
