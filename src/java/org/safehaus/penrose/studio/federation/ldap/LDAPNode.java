package org.safehaus.penrose.studio.federation.ldap;

import org.apache.log4j.Logger;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.federation.FederationNode;
import org.safehaus.penrose.federation.LDAPFederationClient;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.federation.*;
import org.safehaus.penrose.studio.federation.ldap.editor.LDAPEditor;
import org.safehaus.penrose.studio.federation.ldap.editor.LDAPEditorInput;
import org.safehaus.penrose.studio.federation.ldap.repository.LDAPRepositoryNode;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.tree.Node;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class LDAPNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ProjectNode projectNode;
    private FederationNode federationNode;

    private LDAPFederationClient ldapFederation;

    //Map<String,Node> children = new TreeMap<String,Node>();

    public LDAPNode(String name, FederationNode federationNode) {
        super(name, PenroseStudioPlugin.getImage(PenroseImage.FOLDER), null, federationNode);

        this.federationNode = federationNode;
        this.projectNode = federationNode.getProjectNode();

        ldapFederation = new LDAPFederationClient(federationNode.getFederation());
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
    }

    public void open() throws Exception {

        LDAPEditorInput ei = new LDAPEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setLdapFederation(ldapFederation);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, LDAPEditor.class.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        FederationClient federation = federationNode.getFederation();
        Collection<Repository> children = federation.getRepositories("LDAP");
        return !children.isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        FederationClient federation = federationNode.getFederation();
        for (Repository repository : federation.getRepositories("LDAP")) {
            LDAPRepositoryNode node = new LDAPRepositoryNode(
                    repository.getName(),
                    (LDAPRepository)repository,
                    this
            );
            children.add(node);
        }

        return children;
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

    public LDAPFederationClient getLdapFederation() {
        return ldapFederation;
    }

    public void setLdapFederation(LDAPFederationClient ldapFederation) {
        this.ldapFederation = ldapFederation;
    }
}
