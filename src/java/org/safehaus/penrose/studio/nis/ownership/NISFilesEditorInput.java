package org.safehaus.penrose.studio.nis.ownership;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.nis.NISDomain;

/**
 * @author Endi S. Dewata
 */
public class NISFilesEditorInput implements IEditorInput {

    private Project project;
    private NISTool nisTool;
    private NISDomain domain;

    public NISFilesEditorInput() {
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "NIS Users - "+domain.getName();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return "NIS Users - "+domain.getName();
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

        NISFilesEditorInput ei = (NISFilesEditorInput)object;
        if (!equals(project, ei.project)) return false;
        if (!equals(domain, ei.domain)) return false;

        return true;
    }

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }

    public NISTool getNisTool() {
        return nisTool;
    }

    public void setNisTool(NISTool nisTool) {
        this.nisTool = nisTool;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
