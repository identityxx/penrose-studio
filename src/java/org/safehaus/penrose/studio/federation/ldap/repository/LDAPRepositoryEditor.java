package org.safehaus.penrose.studio.federation.ldap.repository;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.studio.federation.ldap.LDAPFederation;
import org.safehaus.penrose.federation.repository.LDAPRepository;

public class LDAPRepositoryEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    LDAPFederation ldapFederation;
    LDAPRepository repository;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        LDAPRepositoryEditorInput ei = (LDAPRepositoryEditorInput)input;
        ldapFederation = ei.getLdapFederation();
        repository = ei.getRepository();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new LDAPRepositorySettingsPage(this));

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

    public LDAPRepository getRepository() {
        return repository;
    }

    public void setRepository(LDAPRepository repository) {
        this.repository = repository;
    }

    public LDAPFederation getLdapFederation() {
        return ldapFederation;
    }
}
