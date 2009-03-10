package org.safehaus.penrose.studio.federation.partition;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FederationDomainEditor extends FormEditor {

    public Logger log = LoggerFactory.getLogger(getClass());

    Server server;
    FederationClient federationClient;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        FederationDomainEditorInput ei = (FederationDomainEditorInput)input;
        server = ei.getServer();
        federationClient = ei.getFederationClient();

        setSite(site);
        setInput(input);
        setPartName(ei.getName());
    }

    public void addPages() {
        try {
            addPage(new FederationDomainSettingsPage(this));
            addPage(new FederationDomainPartitionsPage(this));

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

    public FederationClient getFederationClient() {
        return federationClient;
    }

    public Server getServer() {
        return server;
    }
}