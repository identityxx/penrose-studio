package org.safehaus.penrose.studio.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.nis.editor.NISDomainEditorInput;
import org.safehaus.penrose.studio.nis.editor.NISDomainEditor;
import org.safehaus.penrose.studio.nis.editor.NISDomainDialog;
import org.safehaus.penrose.studio.nis.editor.NISUserDialog;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.partition.Partition;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISDomainNode extends Node {

    ServersView view;
    ProjectNode projectNode;
    NISNode nisNode;

    private NISTool nisTool;
    private NISDomain domain;

    public NISDomainNode(String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);

        domain = (NISDomain)object;
        
        nisNode = (NISNode)parent;
        projectNode = nisNode.getProjectNode();
        view = projectNode.getServersView();

        nisTool = nisNode.getNisTool();
    }

    public Image getImage() {
        Partition partition = nisTool.getPartitions().getPartition(domain.getName());
        return PenrosePlugin.getImage(partition == null ? PenroseImage.RED_FOLDER : PenroseImage.FOLDER);
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

        manager.add(new Action("Edit") {
            public void run() {
                try {
                    edit();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Create Partition") {
            public void run() {
                try {
                    createPartition();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Load Partition") {
            public void run() {
                try {
                    loadPartition();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Unload Partition") {
            public void run() {
                try {
                    unloadPartition();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Remove Partition") {
            public void run() {
                try {
                    removePartition();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new Action("Create Cache") {
            public void run() {
                try {
                    createDatabase();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Load Cache") {
            public void run() {
                try {
                    loadDatabase();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Clear Cache") {
            public void run() {
                try {
                    cleanDatabase();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Remove Cache") {
            public void run() {
                try {
                    removeDatabase();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
/*
        manager.add(new Action("Copy") {
            public void run() {
                try {
                    //copy(connection);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Paste") {
            public void run() {
                try {
                    //paste(connection);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
*/
        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.SIZE_16x16, PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        NISDomainEditorInput ei = new NISDomainEditorInput();
        ei.setProject(projectNode.getProject());
        ei.setNisTool(nisTool);
        ei.setDomain(domain);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISDomainEditor.class.getName());
    }

    public void edit() throws Exception {

        String domainName = domain.getName();

        NISDomainDialog dialog = new NISDomainDialog(view.getSite().getShell(), SWT.NONE);
        dialog.setDomain(domain);
        dialog.open();

        int action = dialog.getAction();
        if (action == NISUserDialog.CANCEL) return;

        nisTool.updateDomain(domainName, domain);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void remove() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Removing NIS Domain",
                "Are you sure?"
        );

        if (!confirm) return;

        Project project = projectNode.getProject();

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof NISDomainNode)) continue;

            NISDomainNode domainNode = (NISDomainNode)node;
            NISDomain domain = domainNode.getDomain();

            try {
                nisTool.removePartition(domain);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            try {
                nisTool.removeCache(domain);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            try {
                nisTool.removePartitionConfig(domain);
                project.removeDirectory("partitions/"+domain.getName());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            try {
                nisTool.removeDomain(domain);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void createPartition() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Creating Partition", "Are you sure?"
        );

        if (!confirm) return;

        Project project = projectNode.getProject();

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof NISDomainNode)) continue;

            NISDomainNode domainNode = (NISDomainNode)node;
            NISDomain domain = domainNode.getDomain();

            nisTool.createPartitionConfig(domain);
            project.upload("partitions/"+domain.getName());
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void loadPartition() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Loading Partition", "Are you sure?"
        );

        if (!confirm) return;

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof NISDomainNode)) continue;

            NISDomainNode domainNode = (NISDomainNode)node;
            NISDomain domain = domainNode.getDomain();

            nisTool.loadPartition(domain);
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void unloadPartition() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Unloading Partition", "Are you sure?"
        );

        if (!confirm) return;

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof NISDomainNode)) continue;

            NISDomainNode domainNode = (NISDomainNode)node;
            NISDomain domain = domainNode.getDomain();

            nisTool.unloadPartition(domain.getName());
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void removePartition() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Removing Partition", "Are you sure?"
        );

        if (!confirm) return;

        Project project = projectNode.getProject();

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof NISDomainNode)) continue;

            NISDomainNode domainNode = (NISDomainNode)node;
            NISDomain domain = domainNode.getDomain();

            nisTool.removePartition(domain);
            nisTool.removePartitionConfig(domain);

            project.removeDirectory("partitions/"+domain.getName());
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void createDatabase() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Creating Cache Database", "Are you sure?"
        );

        if (!confirm) return;

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof NISDomainNode)) continue;

            NISDomainNode domainNode = (NISDomainNode)node;
            NISDomain domain = domainNode.getDomain();

            nisTool.createDatabase(domain);
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void loadDatabase() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Loading Cache Database", "Are you sure?"
        );

        if (!confirm) return;

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof NISDomainNode)) continue;

            NISDomainNode domainNode = (NISDomainNode)node;
            NISDomain domain = domainNode.getDomain();

            nisTool.loadCache(domain);
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void cleanDatabase() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Clearing Cache Database", "Are you sure?"
        );

        if (!confirm) return;

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof NISDomainNode)) continue;

            NISDomainNode domainNode = (NISDomainNode)node;
            NISDomain domain = domainNode.getDomain();

            nisTool.clearCache(domain);
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void removeDatabase() throws Exception {

        boolean confirm = MessageDialog.openQuestion(
                view.getSite().getShell(),
                "Removing Cache Database", "Are you sure?"
        );

        if (!confirm) return;

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof NISDomainNode)) continue;

            NISDomainNode domainNode = (NISDomainNode)node;
            NISDomain domain = domainNode.getDomain();

            nisTool.removeCache(domain);
        }

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }

    public boolean hasChildren() throws Exception {
        return true;
    }

    public Collection<Node> getChildren() throws Exception {
        Collection<Node> children = new ArrayList<Node>();

        NISUsersNode usersNode = new NISUsersNode(
                view,
                "Users",
                "Users",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                null,
                this
        );

        usersNode.setNisTool(nisTool);
        usersNode.setDomain(domain);

        children.add(usersNode);

        NISGroupsNode groupsNode = new NISGroupsNode(
                view,
                "Groups",
                "Groups",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                null,
                this
        );

        groupsNode.setNisTool(nisTool);
        groupsNode.setDomain(domain);

        children.add(groupsNode);

        NISFilesNode filesNode = new NISFilesNode(
                view,
                "Files",
                "Files",
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                null,
                this
        );

        filesNode.setNisTool(nisTool);
        filesNode.setDomain(domain);

        children.add(filesNode);

        return children;
    }

    public NISTool getNisTool() {
        return nisTool;
    }

    public void setNisTool(NISTool nisTool) {
        this.nisTool = nisTool;
    }
}
