package org.safehaus.penrose.studio.federation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.safehaus.penrose.federation.*;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.federation.partition.FederationDomainEditorWizard;
import org.safehaus.penrose.studio.federation.partition.FederationDomainEditor;
import org.safehaus.penrose.studio.federation.partition.FederationDomainEditorInput;
import org.safehaus.penrose.studio.federation.ldap.LDAPNode;
import org.safehaus.penrose.federation.FederationClient;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.studio.federation.jdbc.JDBCNode;
import org.safehaus.penrose.studio.federation.global.GlobalNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.tree.Node;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Endi Sukma Dewata
 */
public class FederationDomainNode extends Node {

    private Project project;
    private FederationClient federationClient;

    public FederationDomainNode(String name, FederationNode federationNode) throws Exception {
        super(name, PenroseStudioPlugin.getImage(PenroseImage.MODULE), null, federationNode);

        project = federationNode.getProject();
        federationClient = new FederationClient(project.getClient(), name);

        refresh();
    }

    public void refresh() throws Exception {

        log.debug("Refreshing repository types:");

        children.clear();

        children.add(new GlobalNode(this));

        for (String type : federationClient.getRepositoryTypes()) {

            log.debug(" - "+type);

            Node node;

            if ("JDBC".equals(type)) {
                node = new JDBCNode(type, this);

            } else if ("LDAP".equals(type)) {
                node = new LDAPNode(type, this);

            } else if ("NIS".equals(type)) {
                node = new NISNode(type, this);

            } else {
                continue;
            }

            children.add(node);
        }
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
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Export") {
            public void run() {
                try {
                    exportFederationConfig();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Edit") {
            public void run() {
                try {
                    edit();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
/*
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
*/

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Refresh") {
            public void run() {
                try {
                    refresh();

                    PenroseStudio penroseStudio = PenroseStudio.getInstance();
                    penroseStudio.notifyChangeListeners();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        FederationDomainEditorInput ei = new FederationDomainEditorInput();
        ei.setProject(project);
        ei.setFederationClient(federationClient);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, FederationDomainEditor.class.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void edit() throws Exception {

        FederationDomainEditorWizard wizard = new FederationDomainEditorWizard(federationClient);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.setPageSize(600, 300);

        if (dialog.open() == Window.CANCEL) return;

        //federationClient.updateGlobalRepository(wizard.getRepository());
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

    public FederationClient getFederationClient() {
        return federationClient;
    }

    public void setFederationClient(FederationClient federationClient) {
        this.federationClient = federationClient;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void importFederationConfig() throws Exception {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        FileDialog dialog = new FileDialog(window.getShell(), SWT.OPEN);
        dialog.setText("Import");
        dialog.setFilterExtensions(new String[] { "*.xml" });

        final String filename = dialog.open();
        if (filename == null) return;

        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();

        progressService.busyCursorWhile(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    monitor.beginTask("Importing Federation configuration...", IProgressMonitor.UNKNOWN);

                    monitor.subTask("Loading "+filename+"...");

                    File file = new File(filename);

                    FederationReader reader = new FederationReader();
                    FederationConfig federationConfig = reader.read(file);

                    monitor.worked(1);

                    monitor.subTask("Uploading Federation configuration...");

                    federationClient.setFederationConfig(federationConfig);
                    federationClient.storeFederationConfig();

                    monitor.worked(1);

                    for (String partitionName : federationClient.getPartitionNames()) {

                        monitor.subTask("Creating "+partitionName+" partition...");

                        federationClient.createPartition(partitionName);
                        federationClient.startPartition(partitionName);

                        monitor.worked(1);
                    }

                } catch (Exception e) {
                    throw new InvocationTargetException(e, e.getMessage());

                } finally {
                    monitor.done();
                }
            }
        });

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void exportFederationConfig() throws Exception {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        FileDialog dialog = new FileDialog(window.getShell(), SWT.SAVE);
        dialog.setText("Export");
        dialog.setFilterExtensions(new String[] { "*.xml" });

        String filename = dialog.open();
        if (filename == null) return;

        FederationConfig federationConfig = federationClient.getFederationConfig();

        File file = new File(filename);

        if (file.exists()) {

            boolean confirm = MessageDialog.openConfirm(
                    window.getShell(),
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
