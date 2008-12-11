package org.safehaus.penrose.studio.project.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.server.ServersView;

public class DisconnectAction extends Action {

    Logger log = Logger.getLogger(getClass());

    public DisconnectAction() {
        setText("&Disconnect");
        setImageDescriptor(PenroseStudio.getImageDescriptor(PenroseImage.DISCONNECT));
        setAccelerator(SWT.CTRL | 'D');
        setToolTipText("Disconnect");
        setId(getClass().getName());
    }

    public void run() {
        try {
            ServersView serversView = ServersView.getInstance();
            ProjectNode projectNode = serversView.getSelectedProjectNode();
            if (projectNode == null) return;

            Project project = projectNode.getProject();
            if (project.isConnected()) projectNode.disconnect();
            
            serversView.close(projectNode);

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

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
            
            Project project = projectNode.getProject();
            return project.isConnected();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }
*/
}
