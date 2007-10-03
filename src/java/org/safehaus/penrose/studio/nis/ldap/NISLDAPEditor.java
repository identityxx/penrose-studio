package org.safehaus.penrose.studio.nis.ldap;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.nis.NISDomain;

public class NISLDAPEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    NISTool nisTool;
    NISDomain domain;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        NISLDAPEditorInput ei = (NISLDAPEditorInput)input;
        nisTool = ei.getNisTool();
        domain = ei.getDomain();

        setSite(site);
        setInput(input);
        setPartName("NIS LDAP - "+domain.getName());
    }

    public void addPages() {
        try {
            addPage(new NISLDAPPage(this));
            addPage(new NISLDAPTrackerPage(this));
            addPage(new NISLDAPErrorsPage(this));

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
