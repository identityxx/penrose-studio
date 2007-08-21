package org.safehaus.penrose.studio.nis.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.studio.nis.NISTool;

public class NISEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    NISTool nisTool;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        NISEditorInput ei = (NISEditorInput)input;
        nisTool = ei.getNisTool();

        setSite(site);
        setInput(input);
        setPartName("NIS");
    }

    public void addPages() {
        try {
            addPage(new NISDatabasePage(this, nisTool));
            addPage(new NISDomainsPage(this, nisTool));
            addPage(new NISUserChangesPage(this, nisTool));
            addPage(new NISGroupChangesPage(this, nisTool));

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

    public NISTool getNisTool() {
        return nisTool;
    }
}
