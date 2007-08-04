package org.safehaus.penrose.studio.logging;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.log4j.Log4jConfig;
import org.safehaus.penrose.log4j.AppenderConfig;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class AppendersNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;

    public AppendersNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("New Appender...") {
            public void run() {
                try {
                    createAppender();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
    }

    public void createAppender() throws Exception {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Log4jConfig loggingConfig = penroseStudio.getLoggingConfig();

        AppenderConfig appenderConfig = new AppenderConfig();

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        AppenderDialog dialog = new AppenderDialog(shell, SWT.NONE);
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
                    ObjectsView.APPENDER,
                    PenrosePlugin.getImage(PenroseImage.APPENDER),
                    appenderConfig,
                    this
            );

            children.add(appenderNode);
        }

        return children;
    }
}
