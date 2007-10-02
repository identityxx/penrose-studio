package org.safehaus.penrose.studio.nis.domain;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.nis.*;
import org.safehaus.penrose.studio.nis.database.NISDatabaseNode;
import org.safehaus.penrose.studio.nis.ldap.NISLDAPNode;
import org.safehaus.penrose.studio.nis.ownership.NISOwnershipNode;
import org.safehaus.penrose.studio.nis.conflict.NISConflictsNode;
import org.safehaus.penrose.studio.nis.linking.NISLinkingNode;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.PartitionConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISDomainNode extends Node {

    ServersView view;
    ProjectNode projectNode;

    private NISNode nisNode;

    private NISTool nisTool;
    private NISDomain domain;

    Collection<Node> children = new ArrayList<Node>();

    NISDatabaseNode  databaseNode;
    NISLDAPNode      ldapNode;
    NISLinkingNode   linkingNode;
    NISConflictsNode conflictsNode;
    NISOwnershipNode ownershipNode;

    public NISDomainNode(String name, String type, Object object, Object parent) {
        super(name, type, PenrosePlugin.getImage(PenroseImage.FOLDER), object, parent);

        domain = (NISDomain)object;

        nisNode = (NISNode)parent;
        projectNode = nisNode.getProjectNode();
        view = projectNode.getServersView();

        nisTool = nisNode.getNisTool();

        databaseNode = new NISDatabaseNode(
                "Database Synchronization",
                ServersView.ENTRY,
                "Database Synchronization",
                this
        );

        children.add(databaseNode);

        ldapNode = new NISLDAPNode(
                "LDAP Synchronization",
                ServersView.ENTRY,
                "LDAP Synchronization",
                this
        );

        children.add(ldapNode);

        linkingNode = new NISLinkingNode(
                "Account Linking",
                ServersView.ENTRY,
                "Account Linking",
                this
        );

        children.add(linkingNode);

        conflictsNode = new NISConflictsNode(
                "Conflict Resolution",
                ServersView.ENTRY,
                "Conflict Resolution",
                this
        );

        children.add(conflictsNode);

        ownershipNode = new NISOwnershipNode(
                "Ownership Alignment",
                ServersView.ENTRY,
                "Ownership Alignment",
                this
        );

        children.add(ownershipNode);
    }

    public Image getImage() {
        Project project = nisTool.getProject();
        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(domain.getName());
        return PenrosePlugin.getImage(partitionConfig == null ? PenroseImage.RED_FOLDER : PenroseImage.FOLDER);

        //Partition partition = nisTool.getPartitions().getPartition(domain.getName());
        //return PenrosePlugin.getImage(partition == null ? PenroseImage.RED_FOLDER : PenroseImage.FOLDER);
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

        NISDomainEditorInput ei = new NISDomainEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setNisTool(nisTool);
        ei.setDomain(domain);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISDomainEditor.class.getName());
    }

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }

    public NISTool getNisTool() {
        return nisTool;
    }

    public void setNisTool(NISTool nisTool) {
        this.nisTool = nisTool;
    }

    public NISNode getNisNode() {
        return nisNode;
    }

    public void setNisNode(NISNode nisNode) {
        this.nisNode = nisNode;
    }

    public boolean hasChildren() throws Exception {
        return !children.isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {
        return children;
    }
}
