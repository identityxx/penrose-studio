package org.safehaus.penrose.studio.source.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.project.ProjectNode;

/**
 * @author Endi S. Dewata
 */
public class SourceEditorInput implements IEditorInput {

    private ProjectNode projectNode;
    private PartitionConfig partitionConfig;
    private SourceConfig sourceConfig;

    public SourceEditorInput() {
    }

    public SourceEditorInput(PartitionConfig partitionConfig, SourceConfig sourceConfig) {
        this.partitionConfig = partitionConfig;
        this.sourceConfig = sourceConfig;
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return sourceConfig.getName();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return sourceConfig.getName();
    }

    public Object getAdapter(Class aClass) {
        return null;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof SourceEditorInput)) return false;

        SourceEditorInput cei = (SourceEditorInput)o;
        return sourceConfig.equals(cei.sourceConfig);
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }
}
