package org.safehaus.penrose.studio.server.editor;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.ServerConfig;

/**
 * @author Endi S. Dewata
 */
public class ServerEditor extends FormEditor {

    public Logger log = Logger.getLogger(getClass());

    private Server server;
    private ServerConfig originalServerConfig;
    private ServerConfig serverConfig;

    private boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);

        setPartName(server.getName());
    }

    public void setInput(IEditorInput input) {
        super.setInput(input);

        ServerEditorInput ei = (ServerEditorInput)input;
        server = ei.getServer();
        originalServerConfig = server.getServerConfig();
        serverConfig = (ServerConfig)originalServerConfig.clone();
    }

    public void addPages() {
        try {
            addPage(new ServerPropertiesPage(this));

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
        try {
            store();
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void doSaveAs() {
    }

    public void store() throws Exception {

        originalServerConfig.copy(serverConfig);
        setPartName(server.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();

        checkDirty();
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void checkDirty() {
        try {
            dirty = false;

            if (!originalServerConfig.equals(serverConfig)) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.debug(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public ServerConfig getOriginalServerConfig() {
        return originalServerConfig;
    }

    public void setOriginalServerConfig(ServerConfig originalServerConfig) {
        this.originalServerConfig = originalServerConfig;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
