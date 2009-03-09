package org.safehaus.penrose.studio.log.tree;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.action.RefreshAction;
import org.safehaus.penrose.studio.log.wizard.AppenderWizard;
import org.safehaus.penrose.log.LogManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.apache.log4j.Logger;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * @author Endi S. Dewata
 */
public class AppendersNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;
    LogsNode logsNode;

    public AppendersNode(ServersView view, String name, LogsNode logsNode) {
        super(name, PenroseStudio.getImage(PenroseImage.FOLDER), null, logsNode);
        this.view = view;
        this.logsNode = logsNode;
    }

    public void update() throws Exception {

        Server server = logsNode.getServerNode().getServer();
        PenroseClient client = server.getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();

        for (String appenderName : logManagerClient.getAppenderNames()) {

            AppenderNode appenderNode = new AppenderNode(
                    view,
                    appenderName,
                    this
            );

            addChild(appenderNode);
        }
    }

    public void expand() throws Exception {
        if (children == null) update();
    }

    public void refresh() throws Exception {
        removeChildren();
        update();
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("New Appender...") {
            public void run() {
                try {
                    createAppender();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(new RefreshAction(this));
    }

    public void createAppender() throws Exception {

        PenroseClient client = logsNode.getServerNode().getServer().getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();

        AppenderWizard wizard = new AppenderWizard();

        WizardDialog dialog = new WizardDialog(view.getSite().getShell(), wizard);
        dialog.setPageSize(600, 300);
        int rc = dialog.open();

        if (rc == Window.CANCEL) return;

        logManagerClient.addAppender(wizard.getAppenderConfig());
        logManagerClient.store();

        ServersView serversView = ServersView.getInstance();
        serversView.refresh(this);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public LogsNode getLogsNode() {
        return logsNode;
    }

    public void setLogsNode(LogsNode logsNode) {
        this.logsNode = logsNode;
    }
}
