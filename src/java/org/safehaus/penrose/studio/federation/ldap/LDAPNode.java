package org.safehaus.penrose.studio.federation.ldap;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.federation.FederationNode;
import org.safehaus.penrose.studio.federation.event.FederationEvent;
import org.safehaus.penrose.studio.federation.event.FederationEventAdapter;
import org.safehaus.penrose.studio.federation.ldap.editor.LDAPEditor;
import org.safehaus.penrose.studio.federation.ldap.editor.LDAPEditorInput;
import org.apache.log4j.Logger;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Endi S. Dewata
 */
public class LDAPNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ProjectNode projectNode;
    private FederationNode federationNode;

    private LDAPFederation ldapFederation;

    Map<String,Node> children = new TreeMap<String,Node>();

    public LDAPNode(String name, FederationNode federationNode) {
        super(name, null, PenrosePlugin.getImage(PenroseImage.FOLDER), null, federationNode);

        this.federationNode = federationNode;
        this.projectNode = federationNode.getProjectNode();

        ldapFederation = federationNode.getFederation().getLdapFederation();

        ldapFederation.addListener(new FederationEventAdapter() {
            public void repositoryAdded(FederationEvent event) {
                LDAPRepository repository = (LDAPRepository)event.getRepository();
                addRepository(repository);

                PenroseStudio penroseStudio = PenroseStudio.getInstance();
                penroseStudio.notifyChangeListeners();
            }
            public void repositoryRemoved(FederationEvent event) {
                LDAPRepository repository = (LDAPRepository)event.getRepository();
                removeRepository(repository.getName());

                PenroseStudio penroseStudio = PenroseStudio.getInstance();
                penroseStudio.notifyChangeListeners();
            }
        });

        refresh();
    }

    public void refresh() {
        for (LDAPRepository repository : ldapFederation.getRepositories()) {
            addRepository(repository);
        }
    }

    public void addRepository(LDAPRepository repository) {

        String name = repository.getName();

        LDAPRepositoryNode node = new LDAPRepositoryNode(
                name,
                repository,
                this
        );

        children.put(name, node);
    }

    public void removeRepository(String name) {
        children.remove(name);
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
        return true;
    }

    public Collection<Node> getChildren() throws Exception {
        return children.values();
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
