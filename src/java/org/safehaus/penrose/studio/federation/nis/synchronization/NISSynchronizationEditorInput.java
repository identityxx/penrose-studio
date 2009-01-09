package org.safehaus.penrose.studio.federation.nis.synchronization;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.federation.NISRepositoryClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;

/**
 * @author Endi S. Dewata
 */
public class NISSynchronizationEditorInput implements IEditorInput {

    private Server project;
    private NISRepositoryClient nisFederationClient;
    private FederationRepositoryConfig domain;

    public NISSynchronizationEditorInput() {
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "Synchronization - "+domain.getName();
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

        NISSynchronizationEditorInput cei = (NISSynchronizationEditorInput)object;
        if (!equals(project, cei.project)) return false;
        if (!equals(domain, cei.domain)) return false;

        return true;
    }

    public FederationRepositoryConfig getDomain() {
        return domain;
    }

    public void setDomain(FederationRepositoryConfig domain) {
        this.domain = domain;
    }

    public NISRepositoryClient getNisFederationClient() {
        return nisFederationClient;
    }

    public void setNisFederationClient(NISRepositoryClient nisFederationClient) {
        this.nisFederationClient = nisFederationClient;
    }

    public Server getProject() {
        return project;
    }

    public void setProject(Server project) {
        this.project = project;
    }
}
