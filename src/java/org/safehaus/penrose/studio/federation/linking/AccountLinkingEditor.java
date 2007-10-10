package org.safehaus.penrose.studio.federation.linking;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.partition.Partition;

public class AccountLinkingEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    private Partition partition;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        AccountLinkingEditorInput ei = (AccountLinkingEditorInput)input;
        partition = ei.getPartition();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new AccountLinkingPage(this));

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

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
