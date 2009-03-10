package org.safehaus.penrose.studio.federation.linking.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

public class IdentityLinkingEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    private Server server;
    private FederationRepositoryConfig repository;
    private String localPartition;
    private String globalPartition;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        IdentityLinkingEditorInput ei = (IdentityLinkingEditorInput)input;
        server = ei.getServer();
        repository = ei.getRepository();
        localPartition = ei.getSourcePartition();
        globalPartition = ei.getTargetPartition();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new IdentityLinkingPage(this));

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

    public FederationRepositoryConfig getRepository() {
        return repository;
    }

    public void setRepository(FederationRepositoryConfig repository) {
        this.repository = repository;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getLocalPartition() {
        return localPartition;
    }

    public void setLocalPartition(String localPartition) {
        this.localPartition = localPartition;
    }

    public String getGlobalPartition() {
        return globalPartition;
    }

    public void setGlobalPartition(String globalPartition) {
        this.globalPartition = globalPartition;
    }
}
