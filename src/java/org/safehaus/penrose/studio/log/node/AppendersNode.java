package org.safehaus.penrose.studio.log.node;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.action.RefreshAction;
import org.safehaus.penrose.studio.log.node.LogsNode;
import org.safehaus.penrose.studio.log.dialog.AppenderDialog;
import org.safehaus.penrose.log.log4j.Log4jConfig;
import org.safehaus.penrose.log.log4j.AppenderConfig;
import org.safehaus.penrose.log.LogManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
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

    public void init() throws Exception {
        update();
    }

    public void update() throws Exception {

        Server server = logsNode.getServerNode().getServer();
        PenroseClient client = server.getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();

        for (String appenderName : logManagerClient.getAppenderConfigNames()) {

            AppenderNode appenderNode = new AppenderNode(
                    view,
                    appenderName,
                    this
            );

            children.add(appenderNode);
        }
    }

    public void refresh() throws Exception {
        children.clear();
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

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Log4jConfig loggingConfig = penroseStudio.getLoggingConfig();

        AppenderConfig appenderConfig = new AppenderConfig();

        AppenderDialog dialog = new AppenderDialog(view.getSite().getShell(), SWT.NONE);
        dialog.setText("Add Appender");
        dialog.setAppenderConfig(appenderConfig);
        dialog.open();

        if (dialog.getAction() == AppenderDialog.CANCEL) return;

        loggingConfig.addAppenderConfig(appenderConfig);

        penroseStudio.notifyChangeListeners();
    }

    public LogsNode getLogsNode() {
        return logsNode;
    }

    public void setLogsNode(LogsNode logsNode) {
        this.logsNode = logsNode;
    }
}
