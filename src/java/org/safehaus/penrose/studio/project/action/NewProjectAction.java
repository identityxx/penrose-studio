package org.safehaus.penrose.studio.project.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.project.ProjectEditorDialog;
import org.safehaus.penrose.studio.object.ObjectsView;

public class NewProjectAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public NewProjectAction() {
        setText("&New...");
        setImageDescriptor(PenrosePlugin.getImageDescriptor(PenroseImage.NEW));
        setAccelerator(SWT.CTRL | 'N');
        setToolTipText("New...");
        setId(getClass().getName());
    }

    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        try {
            Project project = new Project();
            project.setHost("localhost");
            project.setPort(1099);

            ProjectEditorDialog dialog = new ProjectEditorDialog(shell, SWT.NONE);
            dialog.setProject(project);
            dialog.open();

            if (dialog.getAction() == ProjectEditorDialog.CANCEL) return;

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.getApplicationConfig().addProject(project);
            penroseStudio.saveApplicationConfig();

            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());
            objectsView.createProjectNode(project);
            
            penroseStudio.notifyChangeListeners();

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
