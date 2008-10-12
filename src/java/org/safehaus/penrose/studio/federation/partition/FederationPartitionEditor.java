package org.safehaus.penrose.studio.federation.partition;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FederationPartitionEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    Project project;
    FederationClient federationClient;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        FederationPartitionEditorInput ei = (FederationPartitionEditorInput)input;
        project = ei.getProject();
        federationClient = ei.getFederationClient();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new FederationPartitionSettingsPage(this));
            addPage(new FederationPartitionPartitionsPage(this));

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

    public FederationClient getFederationClient() {
        return federationClient;
    }

    public Project getProject() {
        return project;
    }
}