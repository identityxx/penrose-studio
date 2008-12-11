package org.safehaus.penrose.studio.project;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.federation.FederationNode;
import org.safehaus.penrose.studio.logger.LoggingNode;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.browser.BrowserEditorInput;
import org.safehaus.penrose.studio.browser.BrowserEditor;
import org.safehaus.penrose.studio.properties.SystemPropertiesNode;
import org.safehaus.penrose.studio.user.AdministratorNode;
import org.safehaus.penrose.studio.service.ServicesNode;
import org.safehaus.penrose.studio.schema.SchemasNode;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.service.ServiceManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.user.UserConfig;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
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

import java.util.Collection;
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Endi S. Dewata
 */
public class ProjectNode extends Node {

    protected ServersView serversView;

    protected ProjectConfig projectConfig;
    protected Project project;

    Collection<Node> children = new ArrayList<Node>();

    protected PartitionsNode partitionsNode;
    protected SchemasNode schemasNode;
    protected ServicesNode servicesNode;
    protected LoggingNode loggingNode;
    protected FederationNode federationNode;

    public ProjectNode(ServersView serversView, String name, Image image, Object object, Object parent) {
        super(name, image, object, parent);

        this.serversView = serversView;

        projectConfig = (ProjectConfig) object;
        project = new Project(projectConfig);
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Connect") {
            public void run() {
                try {
                    open();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }

            public boolean isEnabled() {
                return !project.isConnected();
            }
        });

        manager.add(new Action("Disconnect") {
            public void run() {
                try {
                    close();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    ErrorDialog.open(e);
                }
            }

            public boolean isEnabled() {
                return project.isConnected();
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
                }
            }

            public boolean isEnabled() {
                return project.isConnected();
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Copy") {
            public void run() {
                try {
                    //copy();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Properties") {
            public void run() {
                try {
                    edit();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {
        if (serversView.isExpanded(this)) {
            disconnect();
            serversView.close(this);

        } else {
            connect();
            serversView.open(this);
        }
    }

    public void expand() throws Exception {
        connect();
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
                    project.connect(monitor);

                    partitionsNode = new PartitionsNode(
                            serversView,
                            "Partitions",
                            "Partitions",
                            ProjectNode.this);
                    children.add(partitionsNode);

                    schemasNode = new SchemasNode(
                            "Schemas",
                            "Schemas",
                            ProjectNode.this);
                    children.add(schemasNode);

                    servicesNode = new ServicesNode(
                            "Services",
                            "Services",
                            ProjectNode.this);
                    children.add(servicesNode);

                    loggingNode = new LoggingNode(
                            serversView,
                            "Logging",
                            "Logging",
                            ProjectNode.this);
                    children.add(loggingNode);

                    federationNode = new FederationNode(
                            "Federation",
                            "Federation",
                            ProjectNode.this);
                    children.add(federationNode);

                    children.add(new AdministratorNode(
                            "Administrator",
                            "Administrator",
                            ProjectNode.this
                    ));

                    children.add(new SystemPropertiesNode(
                            "System Properties",
                            "System Properties",
                            ProjectNode.this
                    ));

                } catch (Exception e) {

                    children.add(new ErrorNode(
                            e.getMessage(),
                            ProjectNode.this
                    ));

                    log.error(e.getMessage(), e);
                    //throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
    }

    public void disconnect() throws Exception {
        try {
            project.close();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //ErrorDialog.open(e);
        }

        children.clear();
    }

    public void edit() throws Exception {

        String oldProjectName = projectConfig.getName();

        Shell shell = serversView.getSite().getShell();

        ProjectDialog dialog = new ProjectDialog(shell, SWT.NONE);
        dialog.setText("Edit Server");
        dialog.setProjectConfig(projectConfig);
        dialog.open();

        if (dialog.getAction() == ProjectDialog.CANCEL) return;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        if (!oldProjectName.equals(projectConfig.getName())) {
            penroseStudio.getApplicationConfig().removeProject(oldProjectName);
            penroseStudio.getApplicationConfig().addProject(projectConfig);
        }

        penroseStudio.saveApplicationConfig();
        penroseStudio.notifyChangeListeners();
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                serversView.getSite().getShell(),
                "Removing Server", "Are you sure?"
        );

        if (!confirm) return;

        serversView.removeProjectConfig(projectConfig.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }
/*
    public void preview() throws Exception {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        PreviewEditorInput ei = new PreviewEditorInput();
        ei.setProject(project);

        page.openEditor(ei, PreviewEditor.class.getName());
    }
*/
    public void browser() throws Exception {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        ProjectConfig projectConfig = project.getProjectConfig();
        String hostname = projectConfig.getHost();

        PenroseClient client = project.getClient();
        ServiceManagerClient serviceManagerClient = client.getServiceManagerClient();
        ServiceConfig serviceConfig = serviceManagerClient.getServiceConfig("LDAP");
        String s = serviceConfig == null ? null : serviceConfig.getParameter("ldapPort");
        int port = s == null ? 10389 : Integer.parseInt(s);

        PenroseClient penroseClient = project.getClient();
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

    public Collection<Node> getChildren() throws Exception {
        return children;
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

    public LoggingNode getLoggingNode() {
        return loggingNode;
    }

    public void setLoggingNode(LoggingNode loggingNode) {
        this.loggingNode = loggingNode;
    }

    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    public void setProjectConfig(ProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
    }

    public ServersView getServersView() {
        return serversView;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
