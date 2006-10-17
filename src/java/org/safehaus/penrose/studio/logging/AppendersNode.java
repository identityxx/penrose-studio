package org.safehaus.penrose.studio.logging;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.project.ProjectNode;
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
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class AppendersNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;
    ProjectNode projectNode;

    public AppendersNode(
            ObjectsView view,
            ProjectNode projectNode,
            String name,
            String type,
            Image image,
            Object object,
            Node parent
    ) {
        super(name, type, image, object, parent);
        this.view = view;
        this.projectNode = projectNode;
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

        Log4jConfig loggingConfig = projectNode.getLog4jConfig();

        AppenderConfig appenderConfig = new AppenderConfig();

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        AppenderDialog dialog = new AppenderDialog(shell, SWT.NONE);
        dialog.setText("Add Appender");
        dialog.setAppenderConfig(appenderConfig);
        dialog.open();

        if (dialog.getAction() == AppenderDialog.CANCEL) return;

        loggingConfig.addAppenderConfig(appenderConfig);

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();
    }

    public boolean hasChildren() throws Exception {
        Log4jConfig loggingConfig = projectNode.getLog4jConfig();
        return !loggingConfig.getAppenderConfigs().isEmpty();
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        Log4jConfig loggingConfig = projectNode.getLog4jConfig();

        for (Iterator i=loggingConfig.getAppenderConfigs().iterator(); i.hasNext(); ) {
            AppenderConfig appenderConfig = (AppenderConfig)i.next();

            AppenderNode appenderNode = new AppenderNode(
                    view,
                    projectNode,
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
