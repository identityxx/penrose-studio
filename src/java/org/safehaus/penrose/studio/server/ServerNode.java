package org.safehaus.penrose.studio.server;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.editor.ServerEditorInput;
import org.safehaus.penrose.studio.server.editor.ServerEditor;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.browser.BrowserEditorInput;
import org.safehaus.penrose.studio.browser.BrowserEditor;
import org.safehaus.penrose.studio.properties.SystemPropertiesNode;
import org.safehaus.penrose.studio.user.AdministratorNode;
import org.safehaus.penrose.studio.engine.EnginesNode;
import org.safehaus.penrose.studio.logging.LoggingNode;
import org.safehaus.penrose.studio.cache.CachesNode;
import org.safehaus.penrose.studio.service.ServicesNode;
import org.safehaus.penrose.studio.schema.SchemasNode;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.user.UserConfig;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchActionConstants;

import java.util.Collection;
import java.util.ArrayList;
import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class ServerNode extends Node {

    Logger log = Logger.getLogger(getClass());

    public final static String LDAP_PORT             = "ldapPort";
    public final static int DEFAULT_LDAP_PORT        = 10389;

    public ServerNode(String name, Object object, Node parent) {
        super(name, PenrosePlugin.getImage(PenroseImage.SERVER), object, parent);
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(actions.getOpenAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getConnectAction());
        manager.add(actions.getDisconnectAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getSaveAction());
        manager.add(actions.getUploadAction());
        manager.add(actions.getRestartAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getPreviewAction());
        manager.add(actions.getBrowserAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getCutAction());
        manager.add(actions.getCopyAction());
        manager.add(actions.getPasteAction());
        manager.add(actions.getDeleteAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getPropertiesAction());
    }

    public boolean isConnected() {
        Server server = getServer();
        return server.isConnected();
    }

    public void open() throws Exception {
        Server server = getServer();

        ServerEditorInput ei = new ServerEditorInput();
        ei.setServer(server);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, ServerEditor.class.getName());
    }

    public void expand() throws Exception {
        if (isConnected()) return;
        connect();
    }
    
    public void connect() throws Exception {
        Server server = getServer();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        server.open();
        penroseStudio.show(this);

        penroseStudio.fireChangeEvent();
    }

    public void disconnect() throws Exception {
        Server server = getServer();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        server.close();

        penroseStudio.fireChangeEvent();
    }

    public void browse() throws Exception {

        Server server = getServer();
        ServerConfig serverConfig = server.getServerConfig();
        PenroseConfig penroseConfig = server.getPenroseConfig();

        String hostname = serverConfig.getHostname();

        ServiceConfig serviceConfig = penroseConfig.getServiceConfig("LDAP");
        String s = serviceConfig.getParameter(LDAP_PORT);
        int port = s == null ? DEFAULT_LDAP_PORT : Integer.parseInt(s);

        UserConfig rootUserConfig = penroseConfig.getRootUserConfig();

        BrowserEditorInput ei = new BrowserEditorInput();
        ei.setProject(serverConfig);
        ei.setHostname(hostname);
        ei.setPort(port);
        ei.setBaseDn("");
        ei.setBindDn(rootUserConfig.getDn());
        ei.setBindPassword(rootUserConfig.getPassword());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = window.getActivePage();
        activePage.openEditor(ei, BrowserEditor.class.getName());
    }

    public Object copy() throws Exception {
        Server server = getServer();
        return server.getServerConfig();
    }

    public boolean canPaste(Object object) throws Exception {
        return getParent().canPaste(object);
    }

    public void paste(Object object) throws Exception {
        getParent().paste(object);
    }

    public void delete() throws Exception {
        Server server = getServer();
        ServerConfig serverConfig = server.getServerConfig();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.removeServer(serverConfig.getName());
        penroseStudio.save();
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection getChildren() throws Exception {
        Collection children = new ArrayList();
        if (!isConnected()) return children;

        children.add(new PartitionsNode(
                getServer(),
                ObjectsView.PARTITIONS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.PARTITIONS,
                this
        ));

        children.add(new SchemasNode(
                getServer(),
                ObjectsView.SCHEMAS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.SCHEMAS,
                this
        ));

        children.add(new ServicesNode(
                getServer(),
                ObjectsView.SERVICES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.SERVICES,
                this
        ));

        children.add(new LoggingNode(
                ObjectsView.LOGGING,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.LOGGING,
                this
        ));

        children.add(new EnginesNode(
                getServer(),
                ObjectsView.ENGINES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.ENGINES,
                this
        ));

        children.add(new AdministratorNode(
                ObjectsView.ADMINISTRATOR,
                PenrosePlugin.getImage(PenroseImage.ADMINISTRATOR),
                ObjectsView.ADMINISTRATOR,
                this
        ));

        children.add(new SystemPropertiesNode(
                ObjectsView.SYSTEM_PROPERTIES,
                PenrosePlugin.getImage(PenroseImage.SYSTEM_PROPERTIES),
                ObjectsView.SYSTEM_PROPERTIES,
                this
        ));

        return children;
    }

    public Server getServer() {
        return (Server)getObject();
    }

    public String getWorkDir() {
        return System.getProperty("user.dir")+File.separator+"work"+File.separator+getName();
    }
}
