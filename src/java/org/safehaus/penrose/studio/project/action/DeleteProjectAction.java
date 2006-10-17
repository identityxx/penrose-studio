package org.safehaus.penrose.studio.project.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.project.ProjectNode;

public class DeleteProjectAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public DeleteProjectAction() {
        setText("&Delete");
        setImageDescriptor(PenrosePlugin.getImageDescriptor(PenroseImage.DELETE));
        setToolTipText("Delete");
        setId(getClass().getName());
    }

    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        try {
            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            ProjectNode projectNode = objectsView.getSelectedProjectNode();
            if (projectNode == null) return;

            projectNode.remove();

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    "Failed creating new project.\n"+
                            "See penrose-studio-log.txt for details."
            );
        }
    }

}
