package org.safehaus.penrose.studio.federation;

import org.safehaus.penrose.studio.plugin.PluginNode;
import org.safehaus.penrose.studio.plugin.PluginsNode;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.studio.federation.ldap.LDAPNode;
import org.safehaus.penrose.studio.federation.wizard.FederationWizard;
import org.safehaus.penrose.studio.federation.editor.FederationEditor;
import org.safehaus.penrose.studio.federation.editor.FederationEditorInput;
//import org.safehaus.penrose.studio.federation.editor.TestEditorInput;
//import org.safehaus.penrose.studio.federation.editor.TestEditor;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.PartitionConfig;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;

import java.util.Collection;
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;

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

        FederationEditorInput ei = new FederationEditorInput();
        ei.setProject(project);
        ei.setFederation(federation);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, FederationEditor.class.getName());

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

        PartitionConfigs partitionConfigs = project.getPartitionConfigs();

        if (partitionConfigs.getPartitionConfig(Federation.PARTITION) == null) {
            boolean b = createPartition();
            if (!b) return;
        }

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        progressService.busyCursorWhile(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    monitor.beginTask("Loading partitions...", IProgressMonitor.UNKNOWN);

                    federation.load(monitor);

                    /*
                    children.add(new GlobalNode(
                            "Global",
                            FederationNode.this
                    ));
                    */
                    children.add(new LDAPNode(
                            "LDAP",
                            FederationNode.this
                    ));

                    children.add(new NISNode(
                            "NIS",
                            FederationNode.this
                    ));

                    started = true;
                    
                } catch (Exception e) {
                    throw new InvocationTargetException(e);

                } finally {
                    monitor.done();
                }
            }
        });
    }

    public boolean createPartition() throws Exception {

        log.debug("Creating Federation partition");

        FederationWizard wizard = new FederationWizard();
        wizard.init(project);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.setPageSize(600, 300);

        if (dialog.open() != Window.OK) return false;

        PartitionConfig partitionConfig = wizard.getPartitionConfig();
        federation.create(partitionConfig);

        return true;
    }

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
}
