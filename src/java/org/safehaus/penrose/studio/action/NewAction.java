package org.safehaus.penrose.studio.action;

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
import org.safehaus.penrose.studio.server.ServerConfig;
import org.safehaus.penrose.studio.server.editor.ServerEditorDialog;

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
            ServerConfig serverConfig = new ServerConfig();
            serverConfig.setHostname("localhost");
            serverConfig.setPort(1099);

            ServerEditorDialog dialog = new ServerEditorDialog(shell, SWT.NONE);
            dialog.setServerConfig(serverConfig);
            dialog.open();

            if (dialog.getAction() == ServerEditorDialog.CANCEL) return;

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.addServer(serverConfig);
            penroseStudio.save();

            penroseStudio.fireChangeEvent();

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    "Failed creating new server.\n"+
                            "See penrose-studio-log.txt for details."
            );
        }
    }

}
