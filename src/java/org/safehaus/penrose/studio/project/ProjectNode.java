package org.safehaus.penrose.studio.project;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.browser.BrowserEditorInput;
import org.safehaus.penrose.studio.browser.BrowserEditor;
import org.safehaus.penrose.studio.preview.PreviewEditorInput;
import org.safehaus.penrose.studio.preview.PreviewEditor;
import org.safehaus.penrose.studio.nis.NISNode;
import org.safehaus.penrose.studio.properties.SystemPropertiesNode;
import org.safehaus.penrose.studio.user.AdministratorNode;
import org.safehaus.penrose.studio.connector.ConnectorNode;
import org.safehaus.penrose.studio.engine.EnginesNode;
import org.safehaus.penrose.studio.handler.HandlersNode;
import org.safehaus.penrose.studio.logging.LoggingNode;
import org.safehaus.penrose.studio.service.ServicesNode;
import org.safehaus.penrose.studio.schema.SchemasNode;
import org.safehaus.penrose.service.ServiceConfigs;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.ldap.LDAPService;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class ProjectNode extends Node {

    protected ServersView view;

    protected ProjectConfig projectConfig;
    protected Project project;

    Collection<Node> children = new ArrayList<Node>();

    protected PartitionsNode partitionsNode;
    protected SchemasNode    schemasNode;
    protected ServicesNode   servicesNode;
    protected LoggingNode    loggingNode;

    public ProjectNode(ServersView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);

        this.view = view;

        projectConfig = (ProjectConfig)object;
        project = new Project(projectConfig);
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            public boolean isEnabled() {
                return !project.isConnected();
            }
        });

        manager.add(new Action("Close") {
            public void run() {
                try {
                    close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            public boolean isEnabled() {
                return project.isConnected();
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

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

        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            public boolean isEnabled() {
                return !project.isConnected();
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
            public boolean isEnabled() {
                return !project.isConnected();
            }
        });
    }

    public void open() throws Exception {
        if (!project.isConnected()) connect();

        view.open(this);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void close() throws Exception {
        disconnect();
        view.close(this);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void connect() throws Exception {
        project.connect();

        partitionsNode = new PartitionsNode(
                view,
                "Partitions",
                "Partitions",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                "Partitions",
                this);
        children.add(partitionsNode);

        schemasNode = new SchemasNode(
                "Schemas",
                "Schemas",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                "Schemas",
                this);
        children.add(schemasNode);

        servicesNode = new ServicesNode(
                "Services",
                "Services",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                "Services",
                this);
        children.add(servicesNode);

        loggingNode = new LoggingNode(
                view,
                "Logging",
                "Logging",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                "Logging",
                this);
        children.add(loggingNode);

        children.add(new HandlersNode(
                view,
                "Handlers",
                "Handlers",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                "Handlers",
                this
        ));

        children.add(new EnginesNode(
                view,
                "Engines",
                "Engines",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                "Engines",
                this
        ));

        children.add(new ConnectorNode(
                view,
                "Connector",
                "Connector",
                PenrosePlugin.getImage(PenroseImage.CONNECTOR),
                "Connector",
                this
        ));

        children.add(new AdministratorNode(
                "Administrator",
                "Administrator",
                PenrosePlugin.getImage(PenroseImage.ADMINISTRATOR),
                "Administrator",
                this
        ));

        children.add(new SystemPropertiesNode(
                "System Properties",
                "System Properties",
                PenrosePlugin.getImage(PenroseImage.SYSTEM_PROPERTIES),
                "System Properties",
                this
        ));

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        if (penroseStudio.getLicense() != null) {
            children.add(new NISNode(
                    "NIS",
                    "NIS",
                    PenrosePlugin.getImage(PenroseImage.MODULE),
                    "NIS",
                    this
            ));
        }
    }

    public void disconnect() throws Exception {
        children.clear();

        project.close();
    }

    public void edit() throws Exception {

        String oldProjectName = projectConfig.getName();

        Shell shell = view.getSite().getShell();
        ProjectEditorDialog dialog = new ProjectEditorDialog(shell, SWT.NONE);
        dialog.setProjectConfig(projectConfig);
        dialog.open();

        if (dialog.getAction() == ProjectEditorDialog.CANCEL) return;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();

        if (!oldProjectName.equals(projectConfig.getName())) {
            penroseStudio.getApplicationConfig().removeProject(oldProjectName);
            penroseStudio.getApplicationConfig().addProject(projectConfig);
        }

        penroseStudio.saveApplicationConfig();
        penroseStudio.notifyChangeListeners();
    }

    public void remove() throws Exception {
        ServersView view = ServersView.getInstance();
        view.removeProjectConfig(projectConfig.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void preview() throws Exception {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        PreviewEditorInput ei = new PreviewEditorInput();
        ei.setProject(project);

        page.openEditor(ei, PreviewEditor.class.getName());
    }

    public void browser() throws Exception {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        ProjectConfig projectConfig = project.getProjectConfig();
        String hostname = projectConfig.getHost();

        ServiceConfigs serviceConfigs = project.getServiceConfigs();
        ServiceConfig serviceConfig = serviceConfigs.getServiceConfig("LDAP");
        String s = serviceConfig == null ? null : serviceConfig.getParameter(LDAPService.LDAP_PORT);
        int port = s == null ? LDAPService.DEFAULT_LDAP_PORT : Integer.parseInt(s);

        PenroseConfig penroseConfig = project.getPenroseConfig();
        String bindDn = penroseConfig.getRootDn().toString();
        byte[] password = penroseConfig.getRootPassword();

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
        if (!project.isConnected()) {
            connect();
        }
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

    public ServersView getView() {
        return view;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
