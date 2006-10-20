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
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.object.ObjectsView;

import java.util.Iterator;

public class CloseAllAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public CloseAllAction() {
        setText("&Close All");
        setToolTipText("Close All");
        setId(getClass().getName());
    }

    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        for (Iterator i=penroseStudio.getProjects().iterator(); i.hasNext(); ) {
            Project project = (Project)i.next();

            try {
                penroseStudio.close(project);

            } catch (Exception e) {
                log.error(e.getMessage(), e);

                MessageDialog.openError(
                        shell,
                        "ERROR",
                        "Failed closing "+project.getName()+".\n"+
                                "See penrose-studio-log.txt for details."
                );
            }
        }
    }

}
