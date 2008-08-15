package org.safehaus.penrose.studio.federation.global.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    Project project;
    FederationClient federation;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        GlobalEditorInput ei = (GlobalEditorInput)input;
        project = ei.getProject();
        federation = ei.getFederation();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new GlobalRepositorySettingsPage(this));
            addPage(new GlobalRepositoryPartitionsPage(this));

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

    public FederationClient getFederation() {
        return federation;
    }
}