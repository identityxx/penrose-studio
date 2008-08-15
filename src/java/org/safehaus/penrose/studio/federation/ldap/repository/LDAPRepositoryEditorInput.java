package org.safehaus.penrose.studio.federation.ldap.repository;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.federation.LDAPFederationClient;
import org.safehaus.penrose.federation.LDAPRepository;

/**
 * @author Endi S. Dewata
 */
public class LDAPRepositoryEditorInput implements IEditorInput {

    private Project project;
    private LDAPFederationClient ldapFederation;
    private LDAPRepository repository;

    public LDAPRepositoryEditorInput() {
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "LDAP - "+repository.getName();
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
                (repository == null ? 0 : repository.hashCode());
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

        LDAPRepositoryEditorInput cei = (LDAPRepositoryEditorInput)object;
        if (!equals(project, cei.project)) return false;
        if (!equals(repository, cei.repository)) return false;

        return true;
    }

    public LDAPRepository getRepository() {
        return repository;
    }

    public void setRepository(LDAPRepository repository) {
        this.repository = repository;
    }

    public LDAPFederationClient getLdapFederation() {
        return ldapFederation;
    }

    public void setLdapFederation(LDAPFederationClient nisFederation) {
        this.ldapFederation = nisFederation;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
