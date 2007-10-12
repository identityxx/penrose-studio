package org.safehaus.penrose.studio.federation.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.federation.nis.conflict.NISConflictsNode;
import org.safehaus.penrose.studio.federation.nis.ownership.NISOwnershipNode;
import org.safehaus.penrose.studio.federation.nis.linking.NISLinkingNode;
import org.safehaus.penrose.studio.federation.nis.ldap.NISLDAPNode;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.nis.database.NISDatabaseNode;
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

    private ProjectNode projectNode;
    private NISNode nisNode;

    private NISFederation nisFederation;
    private NISRepository domain;

    Collection<Node> children = new ArrayList<Node>();

    NISDatabaseNode  databaseNode;
    NISLDAPNode ldapNode;
    NISLinkingNode   linkingNode;
    NISConflictsNode conflictsNode;
    NISOwnershipNode ownershipNode;

    public NISDomainNode(String name, NISRepository domain, NISNode nisNode) {
        super(
                name,
                ServersView.ENTRY,
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                domain,
                nisNode
        );

        this.domain = domain;
        this.nisNode = nisNode;

        nisFederation = nisNode.getNisFederation();
        projectNode = nisNode.getProjectNode();

        databaseNode = new NISDatabaseNode(
                "Database Synchronization",
                this
        );

        children.add(databaseNode);

        ldapNode = new NISLDAPNode(
                "LDAP Synchronization",
                this
        );

        children.add(ldapNode);

        linkingNode = new NISLinkingNode(
                "Identity Linking",
                this
        );

        children.add(linkingNode);

        conflictsNode = new NISConflictsNode(
                "Conflict Resolution",
                this
        );

        children.add(conflictsNode);

        ownershipNode = new NISOwnershipNode(
                "Ownership Alignment",
                this
        );

        children.add(ownershipNode);
    }

    public Image getImage() {
        Project project = nisFederation.getProject();
        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(domain.getName());
        return PenroseStudioPlugin.getImage(partitionConfig == null ? PenroseImage.RED_FOLDER : PenroseImage.FOLDER);

        //Partition partition = nisFederation.getPartitions().getPartition(domain.getName());
        //return PenroseStudioPlugin.getImage(partition == null ? PenroseImage.RED_FOLDER : PenroseImage.FOLDER);
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
        ei.setNisTool(nisFederation);
        ei.setDomain(domain);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISDomainEditor.class.getName());
    }

    public NISRepository getDomain() {
        return domain;
    }

    public void setDomain(NISRepository domain) {
        this.domain = domain;
    }

    public NISFederation getNisTool() {
        return nisFederation;
    }

    public void setNisTool(NISFederation nisFederation) {
        this.nisFederation = nisFederation;
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

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }
}
