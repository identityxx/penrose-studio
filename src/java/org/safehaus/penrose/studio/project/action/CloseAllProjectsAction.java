package org.safehaus.penrose.studio.project.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.swt.widgets.Shell;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.object.ObjectsView;

import java.util.Iterator;

public class CloseAllProjectsAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public CloseAllProjectsAction() {
        setText("&Close All");
        setToolTipText("Close All");
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

        for (Iterator i=objectsView.getProjectNodes().iterator(); i.hasNext(); ) {
            ProjectNode projectNode = (ProjectNode)i.next();

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

}
