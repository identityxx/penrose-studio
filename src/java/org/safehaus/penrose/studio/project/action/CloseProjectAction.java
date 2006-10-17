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
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.object.ObjectsView;

public class CloseProjectAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public CloseProjectAction() {
        setText("&Close");
        setImageDescriptor(PenrosePlugin.getImageDescriptor(PenroseImage.CLOSE));
        setToolTipText("Close");
        setId(getClass().getName());
    }

    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        Shell shell = window.getShell();

        ObjectsView objectsView;

        try {
            objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    "Failed closing project.\n"+
                            "See penrose-studio-log.txt for details."
            );
            return;
        }

        ProjectNode projectNode = objectsView.getSelectedProjectNode();
        if (projectNode == null) return;

        try {
            projectNode.close();

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    "Failed closing "+projectNode.getName()+".\n"+
                            "See penrose-studio-log.txt for details."
            );
        }
    }

}
