package org.safehaus.penrose.studio.action;

import org.eclipse.jface.action.Action;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseStudio;
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
            node.refresh();

            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            penroseStudio.notifyChangeListeners();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
	}

}
