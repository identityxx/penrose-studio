package org.safehaus.penrose.studio.server.tree;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.partition.tree.PartitionsNode;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.log.tree.LogsNode;
import org.safehaus.penrose.studio.util.ApplicationConfig;
import org.safehaus.penrose.studio.server.ServerConfig;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.action.EditServerAction;
import org.safehaus.penrose.studio.federation.FederationNode;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.browser.editor.BrowserEditorInput;
import org.safehaus.penrose.studio.browser.editor.BrowserEditor;
import org.safehaus.penrose.studio.properties.tree.SystemPropertiesNode;
import org.safehaus.penrose.studio.user.tree.AdministratorNode;
import org.safehaus.penrose.studio.service.tree.ServicesNode;
import org.safehaus.penrose.studio.schema.tree.SchemasNode;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.service.ServiceManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.user.UserConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.core.runtime.IProgressMonitor;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Endi S. Dewata
 */
public class ServerNode extends Node {

    protected ServersView serversView;

    private String serverName;
    protected Server server;

    protected PartitionsNode partitionsNode;
    protected SchemasNode schemasNode;
    protected ServicesNode servicesNode;
    protected LogsNode logsNode;
    protected FederationNode federationNode;

    public ServerNode(ServersView serversView, String name, Image image, Object object) {
        super(name, image, object, null);

        this.serverName = name;
        this.serversView = serversView;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Connect") {
            public void run() {
                try {
                    serversView.open(ServerNode.this);

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }

            public boolean isEnabled() {
                return server == null;
            }
        });

        manager.add(new Action("Disconnect") {
            public void run() {
                try {
                    serversView.close(ServerNode.this);

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }

            public boolean isEnabled() {
                return server != null;
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
/*
        manager.add(new Action("Preview") {
            public void run() {
                try {
                    preview();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }

            public boolean isEnabled() {
                return project.isConnected();
            }
        });
*/
        manager.add(new Action("Browser") {
            public void run() {
                try {
                    browser();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }

            public boolean isEnabled() {
                return server != null;
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Copy") {
            public void run() {
                try {
                    //copy();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }

            public boolean isEnabled() {
                return false;
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    //paste();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }

            public boolean isEnabled() {
                return false;
            }
        });

        manager.add(new Action("Delete", PenroseStudio.getImageDescriptor(PenroseImage.DELETE_SMALL)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new EditServerAction(this));
    }

    public void expand() throws Exception {
        if (server == null) connect();
        serversView.setSelection(this);
    }

    public void collapse() throws Exception {
        disconnect();
    }

    public void close() throws Exception {
        disconnect();
        serversView.close(this);
    }

    public void connect() throws Exception {

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        progressService.busyCursorWhile(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    monitor.beginTask("Connecting to "+serverName+"...", 8);

                    monitor.subTask("Connecting to "+serverName+"...");

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    ApplicationConfig applicationConfig = penroseStudio.getApplicationConfig();

                    ServerConfig serverConfig = applicationConfig.getServerConfig(serverName);
                    server = new Server(serverConfig);

                    server.connect(monitor);

                    monitor.worked(1);

                    monitor.subTask("Retrieving partitions...");

                    partitionsNode = new PartitionsNode(
                            serversView,
                            "Partitions",
                            ServerNode.this);
                    partitionsNode.init();

                    addChild(partitionsNode);

                    monitor.worked(1);

                    monitor.subTask("Retrieving schemas...");

                    schemasNode = new SchemasNode(
                            "Schemas",
                            ServerNode.this);
                    schemasNode.init();

                    addChild(schemasNode);

                    monitor.worked(1);

                    monitor.subTask("Retrieving services...");

                    servicesNode = new ServicesNode(
                            "Services",
                            ServerNode.this);
                    servicesNode.init();

                    addChild(servicesNode);

                    monitor.worked(1);

                    monitor.subTask("Retrieving logs...");

                    logsNode = new LogsNode(
                            serversView,
                            "Logs",
                            ServerNode.this);
                    logsNode.init();
                    
                    addChild(logsNode);

                    monitor.worked(1);

                    monitor.subTask("Retrieving federation...");

                    federationNode = new FederationNode(
                            "Federation",
                            ServerNode.this);

                    federationNode.setServer(server);
                    federationNode.init();

                    addChild(federationNode);

                    monitor.worked(1);

                    monitor.subTask("Retrieving administrator...");

                    addChild(new AdministratorNode(
                            "Administrator",
                            ServerNode.this
                    ));

                    monitor.worked(1);

                    monitor.subTask("Retrieving system properties...");

                    addChild(new SystemPropertiesNode(
                            "System Properties",
                            ServerNode.this
                    ));

                    monitor.worked(1);

                } catch (Throwable e) {

                    Throwable t;
                    while (true) {
                        t = e.getCause();
                        if (t == null) break;
                        e = t;
                    }

                    addChild(new ErrorNode(
                            e.getMessage(),
                            ServerNode.this
                    ));

                    log.error(e.getMessage(), e);
                    //throw new RuntimeException(e.getMessage(), t);

                } finally {
                    monitor.done();
                }
            }
        });
    }

    public void disconnect() throws Exception {
        try {
            removeChildren();
            children = null;
            
            if (server != null) {
                server.close();
                server = null;
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //ErrorDialog.open(e);
        }
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                serversView.getSite().getShell(),
                "Removing Server", "Are you sure?"
        );

        if (!confirm) return;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.getApplicationConfig().removeServerConfig(serverName);
        penroseStudio.store();

        serversView.refresh();

        penroseStudio.notifyChangeListeners();
    }

    public void browser() throws Exception {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        ServerConfig projectConfig = server.getServerConfig();
        String hostname = projectConfig.getHost();

        PenroseClient client = server.getClient();
        ServiceManagerClient serviceManagerClient = client.getServiceManagerClient();
        ServiceConfig serviceConfig = serviceManagerClient.getServiceConfig("LDAP");
        String s = serviceConfig == null ? null : serviceConfig.getParameter("ldapPort");
        int port = s == null ? 10389 : Integer.parseInt(s);

        PenroseClient penroseClient = server.getClient();
        UserConfig rootUserConfig = penroseClient.getRootUserConfig();

        String bindDn = rootUserConfig.getDn().toString();
        byte[] password = rootUserConfig.getPassword();

        BrowserEditorInput ei = new BrowserEditorInput();
        ei.setHostname(hostname);
        ei.setPort(port);
        ei.setBindDn(bindDn);
        ei.setPassword(password);

        page.openEditor(ei, BrowserEditor.class.getName());
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public PartitionsNode getPartitionsNode() {
        return partitionsNode;
    }

    public void setPartitionsNode(PartitionsNode partitionsNode) {
        this.partitionsNode = partitionsNode;
    }

    public SchemasNode getSchemasNode() {
        return schemasNode;
    }

    public void setSchemasNode(SchemasNode schemasNode) {
        this.schemasNode = schemasNode;
    }

    public ServicesNode getServicesNode() {
        return servicesNode;
    }

    public void setServicesNode(ServicesNode servicesNode) {
        this.servicesNode = servicesNode;
    }

    public LogsNode getLogsNode() {
        return logsNode;
    }

    public void setLogsNode(LogsNode logsNode) {
        this.logsNode = logsNode;
    }

    public ServersView getServersView() {
        return serversView;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public boolean isConnected() {
        return server != null;
    }
}
