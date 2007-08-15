package org.safehaus.penrose.studio.source.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.project.Project;

/**
 * @author Endi S. Dewata
 */
public class SourceEditorInput implements IEditorInput {

    private Project project;
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

    public int hashCode() {
        return (project == null ? 0 : project.hashCode()) +
                (partitionConfig == null ? 0 : partitionConfig.hashCode()) +
                (sourceConfig == null ? 0 : sourceConfig.hashCode());
    }

    boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return o2.equals(o1);
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        if (object.getClass() != this.getClass()) return false;

        SourceEditorInput ei = (SourceEditorInput)object;
        if (!equals(project, ei.project)) return false;
        if (!equals(partitionConfig, ei.partitionConfig)) return false;
        if (!equals(sourceConfig, ei.sourceConfig)) return false;

        return true;
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
