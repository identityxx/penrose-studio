package org.safehaus.penrose.studio.nis.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.nis.NISDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NISUsersEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    NISTool nisTool;
    NISDomain domain;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        NISUsersEditorInput ei = (NISUsersEditorInput)input;
        nisTool = ei.getNisTool();
        domain = ei.getDomain();

        setSite(site);
        setInput(input);
        setPartName("NIS Users - "+domain.getName());
    }

    public void addPages() {
        try {
            addPage(new NISUsersPage(this));
            addPage(new NISUserScriptsPage(this));
            addPage(new NISUserChangesPage(this));

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

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }

    public NISTool getNisTool() {
        return nisTool;
    }
}
