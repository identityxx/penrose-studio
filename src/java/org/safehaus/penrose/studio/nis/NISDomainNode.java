package org.safehaus.penrose.studio.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.*;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class NISDomainNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    NISTool nisTool;
    NISDomain domain;

    public NISDomainNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;

        domain = (NISDomain)object;
        NISNode nisNode = (NISNode)parent;

        nisTool = nisNode.getNisTool();
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
        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.DELETE)) {
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

        NISEditorInput ei = new NISEditorInput();
        ei.setNisTool(nisTool);
        ei.setDomain(domain);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISEditor.class.getName());
    }

    public void edit() throws Exception {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        NISDomainDialog dialog = new NISDomainDialog(shell, SWT.NONE);
        dialog.setName(domain.getName());
        dialog.setPartition(domain.getPartition());
        dialog.setServer(domain.getServer());
        dialog.setSuffix(domain.getSuffix());
        dialog.open();

        int action = dialog.getAction();
        if (action == NISUserDialog.CANCEL) return;

        String domainName = dialog.getName();
        String partitionName = dialog.getPartition();
        String server = dialog.getServer();
        String suffix = dialog.getSuffix();

        NISDomain newDomain = new NISDomain();
        newDomain.setName(domainName);
        newDomain.setPartition(partitionName);
        newDomain.setServer(server);
        newDomain.setSuffix(suffix);

        nisTool.updateDomain(domain, newDomain);

        domain = newDomain;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public void remove() throws Exception {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        TreeViewer treeViewer = view.getTreeViewer();
        IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();

        boolean confirm = MessageDialog.openQuestion(shell,
                "Confirmation", "Remove selected domains?");

        if (!confirm) return;

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PartitionConfigs partitionConfigs = penroseStudio.getPartitionConfigs();

        for (Iterator i=selection.iterator(); i.hasNext(); ) {
            Node node = (Node)i.next();
            if (!(node instanceof NISDomainNode)) continue;

            NISDomainNode domainNode = (NISDomainNode)node;
            NISDomain domain = domainNode.getDomain();

            String domainName = domain.getName();
            String partitionName = domain.getPartition();

            nisTool.removeDomain(domain);

            partitionConfigs.removePartitionConfig(domainName);
            penroseStudio.removeDirectory("partitions/"+partitionName);
        }

        penroseStudio.notifyChangeListeners();
    }

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }
}
