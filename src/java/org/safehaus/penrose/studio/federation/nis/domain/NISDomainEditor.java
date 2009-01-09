package org.safehaus.penrose.studio.federation.nis.domain;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.federation.NISRepositoryClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NISDomainEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    public Server project;
    public FederationClient federationClient;
    public NISRepositoryClient nisFederationClient;
    public FederationRepositoryConfig repositoryConfig;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        NISDomainEditorInput ei = (NISDomainEditorInput)input;
        project = ei.getProject();
        federationClient = ei.getFederationClient();
        nisFederationClient = ei.getNisFederationClient();
        repositoryConfig = ei.getRepositoryConfig();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            NISDomainSettingsPage page = new NISDomainSettingsPage(this);
            page.setProject(project);
            page.setFederationClient(federationClient);
            page.setNisFederationClient(nisFederationClient);
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

    public NISRepositoryClient getNisFederationClient() {
        return nisFederationClient;
    }
}
