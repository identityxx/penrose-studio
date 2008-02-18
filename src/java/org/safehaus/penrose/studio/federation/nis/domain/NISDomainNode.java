package org.safehaus.penrose.studio.federation.nis.domain;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.federation.nis.conflict.NISConflictsNode;
import org.safehaus.penrose.studio.federation.nis.ownership.NISOwnershipNode;
import org.safehaus.penrose.studio.federation.nis.linking.NISLinkingNode;
import org.safehaus.penrose.studio.federation.nis.ldap.NISLDAPNode;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.federation.nis.database.NISDatabaseNode;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.federation.repository.NISDomain;
import org.safehaus.penrose.studio.federation.nis.yp.NISYPNode;
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
    private NISDomain domain;

    Collection<Node> children = new ArrayList<Node>();

    NISDatabaseNode  databaseNode;
    NISYPNode serverNode;
    NISLDAPNode ldapNode;
    NISLinkingNode   linkingNode;
    NISConflictsNode conflictsNode;
    NISOwnershipNode ownershipNode;

    public NISDomainNode(String name, NISDomain domain, NISNode nisNode) {
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

        if (domain.isYpEnabled()) {

            serverNode = new NISYPNode(
                    "YP Server",
                    this
            );

            children.add(serverNode);
        }

        if (domain.isNisEnabled()) {

            ldapNode = new NISLDAPNode(
                    "LDAP Server",
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

/*
        consolidationNode = new NISConsolidationNode(
                "Stacking Authentication",
                this
        );

        children.add(consolidationNode);
*/
    }

    public Image getImage() {
/*
        Project project = nisFederation.getProject();
        PartitionConfigs partitionConfigs = project.getPartitionConfigs();
        PartitionConfig partitionConfig = partitionConfigs.getPartitionConfig(domain.getName()+"_"+NISFederation.YP);
        return PenroseStudioPlugin.getImage(partitionConfig == null ? PenroseImage.RED_FOLDER : PenroseImage.FOLDER);
*/
        //Partition partition = nisFederation.getPartitions().getPartition(domain.getName());
        //return PenroseStudioPlugin.getImage(partition == null ? PenroseImage.RED_FOLDER : PenroseImage.FOLDER);

        return PenroseStudioPlugin.getImage(PenroseImage.FOLDER);
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
        ei.setNisFederation(nisFederation);
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
