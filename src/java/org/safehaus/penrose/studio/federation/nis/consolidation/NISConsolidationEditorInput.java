package org.safehaus.penrose.studio.federation.nis.consolidation;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.nis.NISDomain;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.partition.Partition;

/**
 * @author Endi S. Dewata
 */
public class NISConsolidationEditorInput implements IEditorInput {

    private Project project;
    private Partition partition;
    private NISDomain domain;
    private NISFederation nisFederation;

    public NISConsolidationEditorInput() {
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "Identity Linking - "+ partition.getName();
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
                (partition == null ? 0 : partition.hashCode());
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

        NISConsolidationEditorInput ei = (NISConsolidationEditorInput)object;
        if (!equals(project, ei.project)) return false;
        if (!equals(partition, ei.partition)) return false;

        return true;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }

    public NISFederation getNisFederation() {
        return nisFederation;
    }

    public void setNisFederation(NISFederation nisFederation) {
        this.nisFederation = nisFederation;
    }
}
