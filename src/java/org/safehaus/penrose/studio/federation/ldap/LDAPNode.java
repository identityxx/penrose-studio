package org.safehaus.penrose.studio.federation.ldap;

import org.apache.log4j.Logger;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.federation.FederationNode;
import org.safehaus.penrose.federation.repository.Repository;
import org.safehaus.penrose.federation.repository.LDAPRepository;
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

    private LDAPFederation ldapFederation;

    //Map<String,Node> children = new TreeMap<String,Node>();

    public LDAPNode(String name, FederationNode federationNode) {
        super(name, PenroseStudioPlugin.getImage(PenroseImage.FOLDER), null, federationNode);

        this.federationNode = federationNode;
        this.projectNode = federationNode.getProjectNode();

        ldapFederation = federationNode.getFederation().getLdapFederation();
    }

    public void removeRepository(String name) {
        //children.remove(name);
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
        Federation federation = federationNode.getFederation();
        Collection<Repository> children = federation.getRepositories("LDAP");
        return !children.isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        Federation federation = federationNode.getFederation();
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

    public LDAPFederation getLdapFederation() {
        return ldapFederation;
    }

    public void setLdapFederation(LDAPFederation ldapFederation) {
        this.ldapFederation = ldapFederation;
    }
}
