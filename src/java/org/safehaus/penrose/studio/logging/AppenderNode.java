package org.safehaus.penrose.studio.logging;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.log4j.AppenderConfig;
import org.safehaus.penrose.log4j.Log4jConfig;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

/**
 * @author Endi S. Dewata
 */
public class AppenderNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ObjectsView view;
    ProjectNode projectNode;
    AppenderConfig appenderConfig;

    public AppenderNode(
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
        this.appenderConfig = (AppenderConfig)object;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Delete", PenrosePlugin.getImageDescriptor(PenroseImage.DELETE)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        Log4jConfig log4jConfig = projectNode.getLog4jConfig();

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        AppenderDialog dialog = new AppenderDialog(shell, SWT.NONE);
        dialog.setText("Edit Appender");
        dialog.setLog4jConfig(log4jConfig);
        dialog.setAppenderConfig(appenderConfig);
        dialog.open();
    }

    public void remove() throws Exception {

        Log4jConfig loggingConfig = projectNode.getLog4jConfig();
        loggingConfig.removeAppenderConfig(appenderConfig.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();
    }
}
