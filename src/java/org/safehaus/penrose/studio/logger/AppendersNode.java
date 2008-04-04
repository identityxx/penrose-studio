package org.safehaus.penrose.studio.logger;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenroseStudioPlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.logger.log4j.Log4jConfig;
import org.safehaus.penrose.logger.log4j.AppenderConfig;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class AppendersNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;

    public AppendersNode(ServersView view, String name, Image image, Object object, Object parent) {
        super(name, image, object, parent);
        this.view = view;
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

    public boolean hasChildren() throws Exception {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Log4jConfig loggingConfig = penroseStudio.getLoggingConfig();
        if (loggingConfig == null) return false;

        return !loggingConfig.getAppenderConfigs().isEmpty();
    }

    public Collection<Node> getChildren() throws Exception {

        Collection<Node> children = new ArrayList<Node>();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Log4jConfig loggingConfig = penroseStudio.getLoggingConfig();

        for (AppenderConfig appenderConfig : loggingConfig.getAppenderConfigs()) {

            AppenderNode appenderNode = new AppenderNode(
                    view,
                    appenderConfig.getName(),
                    PenroseStudioPlugin.getImage(PenroseImage.APPENDER),
                    appenderConfig,
                    this
            );

            children.add(appenderNode);
        }

        return children;
    }
}
