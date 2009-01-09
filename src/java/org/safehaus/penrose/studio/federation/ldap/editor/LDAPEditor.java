package org.safehaus.penrose.studio.federation.ldap.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LDAPEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    private Server project;
    private FederationClient federationClient;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        LDAPEditorInput ei = (LDAPEditorInput)input;
        project = ei.getProject();
        federationClient = ei.getFederationClient();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new LDAPRepositoriesPage(this, federationClient));

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

    public FederationClient getFederationClient() {
        return federationClient;
    }

    public Server getProject() {
        return project;
    }

    public void setProject(Server project) {
        this.project = project;
    }

    public void setFederationClient(FederationClient federationClient) {
        this.federationClient = federationClient;
    }
}
