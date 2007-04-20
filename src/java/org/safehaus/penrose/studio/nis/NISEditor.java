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
        NISEditorInput ei = (NISEditorInput)input;
        //partition = ei.getPartition();
        //origSourceConfig = ei.getSourceConfig();
        //sourceConfig = (SourceConfig)origSourceConfig.clone();

        setSite(site);
        setInput(input);
        setPartName("NIS");
    }

    public void addPages() {
        try {
            addPage(new NISUsersPage(this));
            //addPage(new JDBCSourceBrowsePage(this));
            //addPage(new JDBCSourceCachePage(this));

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
