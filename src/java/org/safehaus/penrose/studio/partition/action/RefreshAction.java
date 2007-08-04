package org.safehaus.penrose.studio.partition.action;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.PenroseStudio;

public class RefreshAction extends Action {

    Logger log = Logger.getLogger(getClass());

    PartitionsNode partitionsNode;

    public RefreshAction(PartitionsNode partitionsNode) {
        this.partitionsNode = partitionsNode;

        setText("&Refresh");
        setId(getClass().getName());
	}

	public void run() {
        try {
            partitionsNode.refresh();

            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            objectsView.show(objectsView.getPartitionsNode());

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
	}

}
