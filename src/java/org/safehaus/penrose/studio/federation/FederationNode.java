package org.safehaus.penrose.studio.federation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.federation.global.GlobalNode;
import org.safehaus.penrose.studio.federation.ldap.LDAPNode;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.studio.plugin.PluginNode;
import org.safehaus.penrose.studio.plugin.PluginsNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.util.FileUtil;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Endi Sukma Dewata
 */
public class FederationNode extends PluginNode {

    protected Collection<Node> children = new ArrayList<Node>();
    protected boolean started;

    private Project project;
    private Federation federation;

    ServersView serversView;

    public FederationNode(PluginsNode parentsNode) throws Exception {
          super("Federation", parentsNode);

        serversView = parentsNode.getView();
        ProjectNode projectNode = parentsNode.getProjectNode();
        project = projectNode.getProject();
    }

    public void showMenu(IMenuManager manager) throws Exception {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Import") {
            public void run() {
                try {
                    importFederationConfig();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Export") {
            public void run() {
                try {
                    exportFederationConfig();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Test") {
            public void run() {
                try {
                    test();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {
        if (!started) start();
/*
        FederationEditorInput ei = new FederationEditorInput();
        ei.setProject(project);
        ei.setFederation(federation);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, FederationEditor.class.getName());
*/
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void expand() throws Exception {
        if (!started) start();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void test() throws Exception {

        BundleContext context = PenroseStudioPlugin.getInstance().getBundleContext();
        for (Bundle bundle : context.getBundles()) {
            log.debug("----------------------------------------");
            log.debug("Symbolic name: "+bundle.getSymbolicName());
            log.debug("ID: "+bundle.getBundleId());
            log.debug("Location: "+bundle.getLocation());
            log.debug("State: "+bundle.getState());
        }

        IEditorInput ei = new IEditorInput() {
            public boolean exists() {
                return true;
            }
            public ImageDescriptor getImageDescriptor() {
                return null;
            }
            public String getName() {
                return "Test";
            }
            public IPersistableElement getPersistable() {
                return null;
            }
            public String getToolTipText() {
                return "Test";
            }
            public Object getAdapter(Class aClass) {
                return null;
            }
        };

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, "org.safehaus.penrose.studio.federation.editor.TestEditor");

    }

    public void start() throws Exception {

        federation = new Federation(project);

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        progressService.busyCursorWhile(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    federation.load(monitor);

                } catch (Exception e) {
                    throw new InvocationTargetException(e, e.getMessage());
                }
            }
        });

        children.add(new GlobalNode(
                "Global",
                FederationNode.this
        ));

        children.add(new LDAPNode(
                "LDAP",
                FederationNode.this
        ));

        children.add(new NISNode(
                "NIS",
                FederationNode.this
        ));

        started = true;
    }

/*
    public boolean createPartition() throws Exception {

        FederationWizard wizard = new FederationWizard();
        wizard.init(project);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.setPageSize(600, 300);

        if (dialog.open() != Window.OK) return false;

        ConnectionConfig connectionConfig = new ConnectionConfig();

        Map<String,String> allParameters = wizard.getAllParameters();
        String url = allParameters.get(JDBCClient.URL);
        url = Helper.replace(url, allParameters);

        Map<String,String> parameters = wizard.getParameters();
        parameters.put(JDBCClient.URL, url);
        
        connectionConfig.setParameters(parameters);

        ConnectionContext connectionContext = new ConnectionContext();
        //connectionContext.setPartition(partition);
        //connectionContext.setAdapter(adapter);

        JDBCConnection connection = new JDBCConnection();
        connection.init(connectionConfig, connectionContext);

        return true;
    }
*/

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {
        return children;
    }

    public Federation getFederation() {
        return federation;
    }

    public void setFederation(Federation federation) {
        this.federation = federation;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void importFederationConfig() throws Exception {

        FileDialog dialog = new FileDialog(getView().getSite().getShell(), SWT.OPEN);
        dialog.setText("Import");
        dialog.setFilterExtensions(new String[] { "*.xml" });

        String filename = dialog.open();
        if (filename == null) return;

        File file = new File(filename);

        File file2 = new File(project.getWorkDir(), "conf"+File.separator+"federation.xml");

        FileUtil.copy(file, file2);
/*
        FederationReader reader = new FederationReader();
        FederationConfig federationConfig = reader.read(file);

        for (RepositoryConfig repository : federationConfig.getRepositoryConfigs()) {
            federation.addRepository(repository);
        }
*/
        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        progressService.busyCursorWhile(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    federation.load(monitor);

                } catch (Exception e) {
                    throw new InvocationTargetException(e, e.getMessage());
                }
            }
        });

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void exportFederationConfig() throws Exception {

        FileDialog dialog = new FileDialog(getView().getSite().getShell(), SWT.SAVE);
        dialog.setText("Export");
        dialog.setFilterExtensions(new String[] { "*.xml" });

        String filename = dialog.open();
        if (filename == null) return;

        FederationConfig federationConfig = federation.getFederationConfig();
/*
        FederationConfig federationConfig = new FederationConfig();

        RepositoryConfig globalRepository = federation.getGlobalRepository();
        globalRepository.setName("GLOBAL");
        globalRepository.setType("GLOBAL");

        federationConfig.addRepositoryConfig(globalRepository);

        LDAPFederation ldapFederation = federation.getLdapFederation();
        for (LDAPRepository repository : ldapFederation.getRepositories()) {
            federationConfig.addRepositoryConfig(repository);
        }

        NISFederation nisFederation = federation.getNisFederation();
        for (NISDomain repository : nisFederation.getRepositories()) {
            federationConfig.addRepositoryConfig(repository);
        }
*/
        File file = new File(filename);

        if (file.exists()) {

            boolean confirm = MessageDialog.openConfirm(
                    getView().getSite().getShell(),
                    "Confirm Export",
                    file.getName()+" already exists.\n"+
                    "Do you want to replace it?"
            );

            if (!confirm) return;
        }

        FederationWriter writer = new FederationWriter();
        writer.write(file, federationConfig);
    }
}
