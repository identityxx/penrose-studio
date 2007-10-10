package org.safehaus.penrose.studio.federation.nis.conflict;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.NISRepository;
import org.safehaus.penrose.studio.project.Project;

/**
 * @author Endi S. Dewata
 */
public class NISGroupsEditorInput implements IEditorInput {

    private Project project;
    private NISFederation nisFederation;
    private NISRepository domain;

    public NISGroupsEditorInput() {
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "NIS Groups - "+domain.getName();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return "NIS Groups - "+domain.getName();
    }

    public Object getAdapter(Class aClass) {
        return null;
    }

    public int hashCode() {
        return (project == null ? 0 : project.hashCode()) +
                (domain == null ? 0 : domain.hashCode());
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

        NISGroupsEditorInput ei = (NISGroupsEditorInput)object;
        if (!equals(project, ei.project)) return false;
        if (!equals(domain, ei.domain)) return false;

        return true;
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
