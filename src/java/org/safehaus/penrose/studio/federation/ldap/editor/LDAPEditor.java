package org.safehaus.penrose.studio.federation.ldap.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.federation.LDAPFederationClient;
import org.safehaus.penrose.studio.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LDAPEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    Project project;
    LDAPFederationClient ldapFederation;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        LDAPEditorInput ei = (LDAPEditorInput)input;
        project = ei.getProject();
        ldapFederation = ei.getLdapFederation();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new LDAPRepositoriesPage(this, ldapFederation));
            addPage(new LDAPPartitionsPage(this, ldapFederation));

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

    public LDAPFederationClient getLdapFederation() {
        return ldapFederation;
    }
}
