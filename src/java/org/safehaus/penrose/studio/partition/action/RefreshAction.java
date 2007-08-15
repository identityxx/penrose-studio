package org.safehaus.penrose.studio.partition.action;

import org.eclipse.jface.action.Action;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.partition.PartitionsNode;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.ProjectNode;

public class RefreshAction extends Action {

    Logger log = Logger.getLogger(getClass());

    PartitionsNode partitionsNode;

    public RefreshAction(PartitionsNode partitionsNode) {
        this.partitionsNode = partitionsNode;

        setText("&Refresh");
        setId(getClass().getName());
	}

	public void run() {
        try {
            partitionsNode.refresh();

            ServersView serversView = ServersView.getInstance();

            ProjectNode projectNode = serversView.getSelectedProjectNode();
            serversView.open(projectNode.getPartitionsNode());

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
	}

}
