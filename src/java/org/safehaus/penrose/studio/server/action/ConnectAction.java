package org.safehaus.penrose.studio.server.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.tree.ServerNode;
import org.safehaus.penrose.studio.server.ServersView;

public class ConnectAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public ConnectAction() {
        setText("&Connect");
        setImageDescriptor(PenroseStudio.getImageDescriptor(PenroseImage.CONNECT));
        setAccelerator(SWT.CTRL | 'C');
        setToolTipText("Connect");
        setId(getClass().getName());
    }

    public void run() {
        try {
            ServersView serversView = ServersView.getInstance();
            ServerNode serverNode = serversView.getSelectedServerNode();
            if (serverNode == null) return;
            if (serverNode.isConnected()) return;

            serversView.open(serverNode);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
/*
    public boolean isEnabled() {
        try {
            ServersView serversView = ServersView.getInstance();
            ProjectNode projectNode = serversView.getSelectedProjectNode();
            if (projectNode == null) return false;

            Project project = projectNode.getServer();
            return !project.isConnected();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
            return false;
        }
    }
*/
}
