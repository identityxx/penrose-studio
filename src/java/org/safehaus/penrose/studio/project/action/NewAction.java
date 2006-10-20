package org.safehaus.penrose.studio.project.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.ProjectConfig;
import org.safehaus.penrose.studio.project.ProjectEditorDialog;

public class NewAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public NewAction() {
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
            ProjectConfig projectConfig = new ProjectConfig();
            projectConfig.setHost("localhost");
            projectConfig.setPort(1099);

            ProjectEditorDialog dialog = new ProjectEditorDialog(shell, SWT.NONE);
            dialog.setProjectConfig(projectConfig);
            dialog.open();

            if (dialog.getAction() == ProjectEditorDialog.CANCEL) return;

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.addProject(projectConfig);
            penroseStudio.save();

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
