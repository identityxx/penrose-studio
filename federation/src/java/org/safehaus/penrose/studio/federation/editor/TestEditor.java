package org.safehaus.penrose.studio.federation.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {

        log.debug("Creating Federation Editor");
        
        setSite(site);
        setInput(input);
        setPartName("Federation");
    }

    public void addPages() {
        try {

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
}
