package org.safehaus.penrose.studio.federation;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionConfig;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class FederationNode extends Node {

    Logger log = Logger.getLogger(getClass());

    private Project project;
    private ProjectNode projectNode;

    protected Collection<Node> children = new ArrayList<Node>();

    public FederationNode(String name, Object object, ProjectNode projectNode) throws Exception {
        super(name, PenroseStudioPlugin.getImage(PenroseImage.FOLDER), object, projectNode);

        this.projectNode = projectNode;
        project = projectNode.getProject();

        refresh();
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new Action("Refresh") {
            public void run() {
                try {
                    refresh();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void refresh() throws Exception {
        children.clear();

        PartitionManagerClient partitionManagerClient = project.getClient().getPartitionManagerClient();

        for (String partitionName : partitionManagerClient.getPartitionNames()) {
            log.debug("Partition "+partitionName+":");

            PartitionConfig partitionConfig = partitionManagerClient.getPartitionConfig(partitionName);
            if (partitionConfig == null) continue;
            
            String partitionClass = partitionConfig.getPartitionClass();
            log.debug(" - Class: "+partitionClass);

            if (!"org.safehaus.penrose.federation.partition.FederationPartition".equals(partitionClass)) continue;

            FederationDomainNode federationDomainNode = new FederationDomainNode(partitionName, this);
            children.add(federationDomainNode);
        }
    }

    public boolean hasChildren() throws Exception {
        return !children.isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {
        return children;
    }

    public Project getProject() {
        return project;
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }
}
