package org.safehaus.penrose.studio.federation.nis.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.project.Project;

public class NISEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    public Project project;
    public FederationClient federationClient;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        NISEditorInput ei = (NISEditorInput)input;
        project = ei.getProject();
        federationClient = ei.getFederationClient();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new NISDomainsPage(this, federationClient));
            addPage(new NISYPConfPage(this, federationClient));

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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
