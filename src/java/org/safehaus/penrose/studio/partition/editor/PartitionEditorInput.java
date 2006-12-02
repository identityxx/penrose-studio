package org.safehaus.penrose.studio.partition.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.partition.PartitionConfig;

/**
 * @author Endi S. Dewata
 */
public class PartitionEditorInput implements IEditorInput {

    private Server server;
    private PartitionConfig partitionConfig;

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return partitionConfig.getName();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return partitionConfig.getName();
    }

    public Object getAdapter(Class aClass) {
        return null;
    }

    public int hashCode() {
        return (partitionConfig == null ? 0 : partitionConfig.hashCode());
    }

    boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 != null) return o1.equals(o2);
        return o2.equals(o1);
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof PartitionEditorInput)) return false;

        PartitionEditorInput cei = (PartitionEditorInput)o;
        if (!equals(partitionConfig, cei.partitionConfig)) return false;

        return true;
    }

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
