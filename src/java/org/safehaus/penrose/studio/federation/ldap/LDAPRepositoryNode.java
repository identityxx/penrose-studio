package org.safehaus.penrose.studio.federation.ldap;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.federation.ldap.linking.LDAPLinkingNode;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class LDAPRepositoryNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private ProjectNode projectNode;
    private LDAPNode ldapNode;

    private LDAPRepository repository;

    Collection<Node> children = new ArrayList<Node>();

    LDAPLinkingNode   linkingNode;

    public LDAPRepositoryNode(String name, LDAPRepository repository, LDAPNode ldapNode) {
        super(
                name,
                ServersView.ENTRY,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                repository,
                ldapNode
        );

        this.repository = repository;
        this.ldapNode = ldapNode;

        projectNode = ldapNode.getProjectNode();

        linkingNode = new LDAPLinkingNode(
                "Account Linking",
                this
        );

        children.add(linkingNode);
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
