package org.safehaus.penrose.studio.federation.ldap.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;

/**
 * @author Endi S. Dewata
 */
public class LDAPEditorInput implements IEditorInput {

    private Project project;
    private LDAPFederation ldapFederation;

    public LDAPEditorInput() {
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "LDAP";
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return "LDAP";
    }

    public Object getAdapter(Class aClass) {
        return null;
    }

    public int hashCode() {
        return project == null ? 0 : project.hashCode();
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

        LDAPEditorInput ei = (LDAPEditorInput)object;
        if (!equals(project, ei.project)) return false;

        return true;
    }

    public LDAPFederation getLdapFederation() {
        return ldapFederation;
    }

    public void setLdapFederation(LDAPFederation nisFederation) {
        this.ldapFederation = nisFederation;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
