package org.safehaus.penrose.studio.federation.nis.ownership;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.federation.NISRepositoryClient;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.federation.nis.editor.NISHostsPage;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NISFilesEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    Server server;
    NISRepositoryClient nisFederation;
    FederationRepositoryConfig domain;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        NISFilesEditorInput ei = (NISFilesEditorInput)input;
        server = ei.getServer();
        nisFederation = ei.getNisTool();
        domain = ei.getDomain();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new NISHostsPage(this));
            addPage(new NISFilesPage(this));
            addPage(new NISScriptsPage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
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

    public FederationRepositoryConfig getDomain() {
        return domain;
    }

    public void setDomain(FederationRepositoryConfig domain) {
        this.domain = domain;
    }

    public NISRepositoryClient getNisTool() {
        return nisFederation;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
