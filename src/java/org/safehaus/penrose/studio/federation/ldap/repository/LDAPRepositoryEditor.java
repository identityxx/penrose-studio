package org.safehaus.penrose.studio.federation.ldap.repository;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.federation.LDAPFederationClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LDAPRepositoryEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    Project project;
    FederationClient federationClient;
    LDAPFederationClient ldapFederationClient;
    FederationRepositoryConfig repositoryConfig;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        LDAPRepositoryEditorInput ei = (LDAPRepositoryEditorInput)input;
        project = ei.getProject();
        federationClient = ei.getFederationClient();
        ldapFederationClient = ei.getLdapFederationClient();
        repositoryConfig = ei.getRepositoryConfig();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            LDAPRepositorySettingsPage page = new LDAPRepositorySettingsPage(this);
            page.setProject(project);
            page.setFederationClient(federationClient);
            page.setLdapFederationClient(ldapFederationClient);
            page.setRepositoryConfig(repositoryConfig);
            addPage(page);

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

    public FederationRepositoryConfig getRepositoryConfig() {
        return repositoryConfig;
    }

    public void setRepositoryConfig(FederationRepositoryConfig repositoryConfig) {
        this.repositoryConfig = repositoryConfig;
    }

    public LDAPFederationClient getLdapFederationClient() {
        return ldapFederationClient;
    }
}
