package org.safehaus.penrose.studio.federation.nis.consolidation;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.federation.nis.*;
import org.safehaus.penrose.studio.federation.nis.domain.NISDomainNode;
import org.safehaus.penrose.partition.Partition;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Endi Sukma Dewata
 */
public class NISConsolidationNode extends Node {

    private NISDomainNode domainNode;
    private NISFederation nisFederation;

    public NISConsolidationNode(String name, NISDomainNode domainNode) {
        super(
                name,
                ServersView.ENTRY,
                PenroseStudioPlugin.getImage(PenroseImage.FOLDER),
                null,
                domainNode
        );

        this.domainNode = domainNode;

        NISNode nisNode = domainNode.getNisNode();
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

        NISDomain repository = domainNode.getDomain();
        Partition partition = nisFederation.getPartitions().getPartition(repository.getName());

        NISConsolidationEditorInput ei = new NISConsolidationEditorInput();
        ei.setPartition(partition);
        ei.setDomain(repository);
        ei.setNisFederation(nisFederation);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISConsolidationEditor.class.getName());
    }

}
