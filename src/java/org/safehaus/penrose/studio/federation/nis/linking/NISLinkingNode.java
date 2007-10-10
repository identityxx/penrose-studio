package org.safehaus.penrose.studio.federation.nis.linking;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.federation.nis.NISNode;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.NISDomainNode;
import org.safehaus.penrose.studio.federation.linking.LinkingEditorInput;
import org.safehaus.penrose.studio.federation.linking.LinkingEditor;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.partition.Partition;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Endi S. Dewata
 */
public class NISLinkingNode extends Node {

    ProjectNode projectNode;
    NISNode nisNode;
    NISDomainNode domainNode;

    private NISFederation nisFederation;

    public NISLinkingNode(String name, NISDomainNode domainNode) {
        super(
                name,
                ServersView.ENTRY,
                PenrosePlugin.getImage(PenroseImage.FOLDER),
                null,
                domainNode
        );

        this.domainNode = domainNode;
        
        nisNode = domainNode.getNisNode();
        projectNode = nisNode.getProjectNode();

        nisFederation = nisNode.getNisFederation();
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
    }

    public void open() throws Exception {
/*
        NISLinkEditorInput ei = new NISLinkEditorInput();
        ei.setNisFederation(nisFederation);
        ei.etDomain(domainNode.getDomain());

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISLinkEditor.class.getName());
*/
        Partition partition = nisFederation.getPartitions().getPartition(domainNode.getName());

        LinkingEditorInput ei = new LinkingEditorInput();
        ei.setPartition(partition);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, LinkingEditor.class.getName());
    }

    public NISFederation getNisTool() {
        return nisFederation;
    }

    public void setNisTool(NISFederation nisFederation) {
        this.nisFederation = nisFederation;
    }
}
