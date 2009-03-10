package org.safehaus.penrose.studio.federation.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

public class FederationEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    FederationClient federation;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        FederationEditorInput ei = (FederationEditorInput)input;
        federation = ei.getFederation();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new FederationDatabasePage(this, federation));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
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
