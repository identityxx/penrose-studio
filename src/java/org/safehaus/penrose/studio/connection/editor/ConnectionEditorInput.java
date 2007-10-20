package org.safehaus.penrose.studio.connection.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.studio.project.Project;

/**
 * @author Endi S. Dewata
 */
public class ConnectionEditorInput implements IEditorInput {

    private Project project;
    private PartitionConfig partitionConfig;
    private ConnectionConfig connectionConfig;

    public ConnectionEditorInput() {
    }

    public ConnectionEditorInput(PartitionConfig partitionConfig, ConnectionConfig connectionConfig) {
        this.partitionConfig = partitionConfig;
        this.connectionConfig = connectionConfig;
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return partitionConfig.getName()+"/"+connectionConfig.getName();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return getName();
    }

    public Object getAdapter(Class aClass) {
        return null;
    }

    public int hashCode() {
        return (project == null ? 0 : project.hashCode()) +
                (partitionConfig == null ? 0 : partitionConfig.hashCode()) +
                (connectionConfig == null ? 0 : connectionConfig.hashCode());
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

        ConnectionEditorInput ei = (ConnectionEditorInput)object;
        if (!equals(project, ei.project)) return false;
        if (!equals(partitionConfig, ei.partitionConfig)) return false;
        if (!equals(connectionConfig, ei.connectionConfig)) return false;

        return true;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
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
