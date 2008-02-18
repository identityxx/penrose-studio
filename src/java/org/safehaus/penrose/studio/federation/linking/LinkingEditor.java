package org.safehaus.penrose.studio.federation.linking;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.federation.repository.Repository;
import org.safehaus.penrose.studio.project.Project;

public class LinkingEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    private Project project;
    private Repository repository;
    private String partitionName;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        LinkingEditorInput ei = (LinkingEditorInput)input;
        project = ei.getProject();
        repository = ei.getRepository();
        partitionName = ei.getPartitionName();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new LinkingPage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
    }

    public void doSaveAs() {
    }

    public boolean isDirty() {
        return false;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }
}
