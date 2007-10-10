package org.safehaus.penrose.studio.federation.global;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.federation.FederationNode;
import org.safehaus.penrose.studio.federation.ldap.*;
import org.safehaus.penrose.studio.federation.ldap.editor.LDAPEditorInput;
import org.safehaus.penrose.studio.federation.ldap.editor.LDAPEditor;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.apache.log4j.Logger;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Endi S. Dewata
 */
public class GlobalNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ProjectNode projectNode;
    private FederationNode federationNode;

    private LDAPFederation ldapFederation;

    public GlobalNode(String name, FederationNode federationNode) {
        super(name, null, PenrosePlugin.getImage(PenroseImage.FOLDER), null, federationNode);

        this.federationNode = federationNode;
        this.projectNode = federationNode.getProjectNode();

        ldapFederation = federationNode.getFederation().getLdapFederation();

        refresh();
    }

    public void refresh() {
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
