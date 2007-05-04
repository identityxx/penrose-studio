package org.safehaus.penrose.studio.nis;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.apache.log4j.Logger;

public class NISEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {

        setSite(site);
        setInput(input);
        setPartName("NIS");
    }

    public void addPages() {
        try {
            addPage(new NISDomainsPage(this));
            addPage(new NISUsersPage(this));
            addPage(new NISGroupsPage(this));
            addPage(new NISHostsPage(this));
            addPage(new NISChangesPage(this));
            addPage(new NISFilesPage(this));

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
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
