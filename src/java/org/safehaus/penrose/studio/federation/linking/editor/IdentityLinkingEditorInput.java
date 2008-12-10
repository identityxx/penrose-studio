package org.safehaus.penrose.studio.federation.linking.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.federation.FederationRepositoryConfig;

/**
 * @author Endi S. Dewata
 */
public class IdentityLinkingEditorInput implements IEditorInput {

    private Project project;
    private FederationRepositoryConfig repository;
    private String sourcePartition;
    private String targetPartition;

    public IdentityLinkingEditorInput() {
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "Identity Linking - "+ repository.getName();
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
                (repository == null ? 0 : repository.hashCode()) +
                (sourcePartition == null ? 0 : sourcePartition.hashCode()) +
                (targetPartition == null ? 0 : targetPartition.hashCode());
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

        IdentityLinkingEditorInput ei = (IdentityLinkingEditorInput)object;
        if (!equals(project, ei.project)) return false;
        if (!equals(repository, ei.repository)) return false;
        if (!equals(sourcePartition, ei.sourcePartition)) return false;
        if (!equals(targetPartition, ei.targetPartition)) return false;

        return true;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public FederationRepositoryConfig getRepository() {
        return repository;
    }

    public void setRepository(FederationRepositoryConfig repository) {
        this.repository = repository;
    }

    public String getSourcePartition() {
        return sourcePartition;
    }

    public void setSourcePartition(String sourcePartition) {
        this.sourcePartition = sourcePartition;
    }

    public String getTargetPartition() {
        return targetPartition;
    }

    public void setTargetPartition(String targetPartition) {
        this.targetPartition = targetPartition;
    }
}
