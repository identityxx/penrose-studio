package org.safehaus.penrose.studio.project;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.util.PenroseStudioClipboard;
import org.safehaus.penrose.studio.preview.PreviewEditorInput;
import org.safehaus.penrose.studio.preview.PreviewEditor;
import org.safehaus.penrose.studio.browser.BrowserEditorInput;
import org.safehaus.penrose.studio.browser.BrowserEditor;
import org.safehaus.penrose.studio.properties.SystemPropertiesNode;
import org.safehaus.penrose.studio.user.AdministratorNode;
import org.safehaus.penrose.studio.connector.ConnectorNode;
import org.safehaus.penrose.studio.engine.EnginesNode;
import org.safehaus.penrose.studio.logging.LoggingNode;
import org.safehaus.penrose.studio.cache.CachesNode;
import org.safehaus.penrose.studio.service.ServicesNode;
import org.safehaus.penrose.studio.schema.SchemasNode;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.service.ServiceConfig;
import org.safehaus.penrose.user.UserConfig;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
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
public class ProjectNode extends Node {

    Logger log = Logger.getLogger(getClass());

    public final static String LDAP_PORT             = "ldapPort";
    public final static int DEFAULT_LDAP_PORT        = 10389;

    ObjectsView view;

    public ProjectNode(ObjectsView view, String name, String type, Object object, Node parent) {
        super(name, type, PenrosePlugin.getImage(PenroseImage.SERVER), object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(actions.getOpenAction());
        manager.add(actions.getCloseAction());

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
        Project project = getProject();
        return project.isConnected();
    }

    public void open() throws Exception {
        if (isConnected()) return;

        Project project = getProject();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.open(project);
        penroseStudio.show(this);
    }

    public void browse() throws Exception {

        Project project = getProject();
        ProjectConfig projectConfig = project.getProjectConfig();
        PenroseConfig penroseConfig = project.getPenroseConfig();

        String hostname = projectConfig.getHost();

        ServiceConfig serviceConfig = penroseConfig.getServiceConfig("LDAP");
        String s = serviceConfig.getParameter(LDAP_PORT);
        int port = s == null ? DEFAULT_LDAP_PORT : Integer.parseInt(s);

        UserConfig rootUserConfig = penroseConfig.getRootUserConfig();

        BrowserEditorInput ei = new BrowserEditorInput();
        ei.setProject(projectConfig);
        ei.setHostname(hostname);
        ei.setPort(port);
        ei.setBaseDn("");
        ei.setBindDn(rootUserConfig.getDn());
        ei.setBindPassword(rootUserConfig.getPassword());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = window.getActivePage();
        activePage.openEditor(ei, BrowserEditor.class.getName());
    }

    public void remove() throws Exception {

        Project project = getProject();
        ProjectConfig projectConfig = project.getProjectConfig();

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        boolean confirm = MessageDialog.openQuestion(
                shell,
                "Confirmation",
                "Remove Project \""+projectConfig.getName()+"\"?");

        if (!confirm) return;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.removeProject(projectConfig.getName());
        penroseStudio.save();
    }

    public void copy(PenroseStudioClipboard clipboard) throws Exception {
        Project project = getProject();
        clipboard.put(project.getProjectConfig());
    }

    public void paste(PenroseStudioClipboard clipboard) throws Exception {
        getParent().paste(clipboard);
    }

    public boolean hasChildren() throws Exception {
        return isConnected();
    }

    public Collection getChildren() throws Exception {
        Collection children = new ArrayList();
        if (!isConnected()) return children;

        children.add(new PartitionsNode(
                view,
                ObjectsView.PARTITIONS,
                ObjectsView.PARTITIONS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.PARTITIONS,
                this
        ));

        children.add(new SchemasNode(
                view,
                ObjectsView.SCHEMAS,
                ObjectsView.SCHEMAS,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.SCHEMAS,
                this
        ));

        children.add(new ServicesNode(
                view,
                ObjectsView.SERVICES,
                ObjectsView.SERVICES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.SERVICES,
                this
        ));

        children.add(new CachesNode(
                view,
                ObjectsView.CACHES,
                ObjectsView.CACHES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.CACHES,
                this
        ));

        children.add(new LoggingNode(
                view,
                ObjectsView.LOGGING,
                ObjectsView.LOGGING,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.LOGGING,
                this
        ));

        children.add(new EnginesNode(
                view,
                ObjectsView.ENGINES,
                ObjectsView.ENGINES,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                ObjectsView.ENGINES,
                this
        ));

        children.add(new ConnectorNode(
                view,
                ObjectsView.CONNECTOR,
                ObjectsView.CONNECTOR,
                PenrosePlugin.getImage(PenroseImage.CONNECTOR),
                ObjectsView.CONNECTOR,
                this
        ));

        children.add(new AdministratorNode(
                view,
                ObjectsView.ADMINISTRATOR,
                ObjectsView.ADMINISTRATOR,
                PenrosePlugin.getImage(PenroseImage.ADMINISTRATOR),
                ObjectsView.ADMINISTRATOR,
                this
        ));

        children.add(new SystemPropertiesNode(
                view,
                ObjectsView.SYSTEM_PROPERTIES,
                ObjectsView.SYSTEM_PROPERTIES,
                PenrosePlugin.getImage(PenroseImage.SYSTEM_PROPERTIES),
                ObjectsView.SYSTEM_PROPERTIES,
                this
        ));

        Project project = getProject();
        log.debug("["+project.getName()+"] getChildren: "+children.size());

        return children;
    }

    public Project getProject() {
        return (Project)getObject();
    }

    public String getWorkDir() {
        return System.getProperty("user.dir")+File.separator+"work"+File.separator+getName();
    }
}
