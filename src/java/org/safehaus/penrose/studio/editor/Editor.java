package org.safehaus.penrose.studio.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.apache.log4j.Logger;

/**
 * @author Endi Sukma Dewata
 */
public abstract class Editor extends FormEditor {

    public Logger log = Logger.getLogger(getClass());

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        setPartName(input.getName());

        init();
    }

    public void init() throws PartInitException {
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
    }

    public void doSaveAs() {
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void store() throws Exception {

    }
}
