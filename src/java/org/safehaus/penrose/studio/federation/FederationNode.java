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

    protected Collection<Node> children;

    public FederationNode(String name, Object object, ProjectNode projectNode) throws Exception {
        super(name, PenroseStudioPlugin.getImage(PenroseImage.FOLDER), object, projectNode);

        this.projectNode = projectNode;
        project = projectNode.getProject();
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new Action("Refresh") {
            public void run() {
                try {
                    refresh();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void refresh() throws Exception {
        children = new ArrayList<Node>();

        PartitionManagerClient partitionManagerClient = project.getClient().getPartitionManagerClient();

        for (String partitionName : partitionManagerClient.getPartitionNames()) {
            PartitionConfig partitionConfig = partitionManagerClient.getPartitionConfig(partitionName);
            String partitionClass = partitionConfig.getPartitionClass();
            log.debug("Partition "+partitionName+": "+partitionClass);

            if (!"org.safehaus.penrose.federation.partition.FederationPartition".equals(partitionClass)) continue;

            FederationPartitionNode federationPartitionNode = new FederationPartitionNode(partitionName, this);
            children.add(federationPartitionNode);
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {
        if (children == null) refresh();
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
