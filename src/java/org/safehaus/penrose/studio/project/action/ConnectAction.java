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
            ProjectNode projectNode = serversView.getSelectedProjectNode();
            if (projectNode == null) return;

            Project project = projectNode.getProject();
            if (project.isConnected()) return;

            projectNode.connect();
            serversView.open(projectNode);

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
            return !project.isConnected();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
            return false;
        }
    }
*/
}
