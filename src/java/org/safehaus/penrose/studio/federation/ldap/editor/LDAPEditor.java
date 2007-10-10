package org.safehaus.penrose.studio.federation.ldap.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.studio.federation.ldap.editor.LDAPRepositoriesPage;

public class LDAPEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    LDAPFederation ldapFederation;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        LDAPEditorInput ei = (LDAPEditorInput)input;
        ldapFederation = ei.getLdapFederation();

        setSite(site);
        setInput(input);
        setPartName("LDAP");
    }

    public void addPages() {
        try {
            addPage(new LDAPRepositoriesPage(this, ldapFederation));

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

    public LDAPFederation getLdapFederation() {
        return ldapFederation;
    }
}
