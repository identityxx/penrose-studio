package org.safehaus.penrose.studio.server.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.Shell;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.ServerNode;

import java.util.Iterator;

public class DisconnectAllAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public DisconnectAllAction() {
        setText("&Disconnect All");
        setToolTipText("Disconnect All");
        setId(getClass().getName());
    }

    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        for (Iterator i=penroseStudio.getServerNodes().iterator(); i.hasNext(); ) {
            ServerNode serverNode = (ServerNode)i.next();

            try {
                serverNode.disconnect();

            } catch (Exception e) {
                log.error(e.getMessage(), e);

                MessageDialog.openError(
                        shell,
                        "ERROR",
                        "Failed closing "+serverNode.getName()+".\n"+
                                "See penrose-studio-log.txt for details."
                );
            }
        }
    }

}
