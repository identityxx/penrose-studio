package org.safehaus.penrose.studio.action;

import org.eclipse.jface.action.Action;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.tree.Node;

public class RefreshAction extends Action {

    Logger log = Logger.getLogger(getClass());

    Node node;

    public RefreshAction(Node node) {
        this.node = node;

        setText("&Refresh");
        setId(getClass().getName());
	}

	public void run() {
        try {
            ServersView serversView = ServersView.getInstance();
            serversView.refresh(node);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
	}

}
