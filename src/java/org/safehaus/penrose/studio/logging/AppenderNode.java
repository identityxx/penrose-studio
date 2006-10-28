package org.safehaus.penrose.studio.logging;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.logging.editor.AppenderDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.log4j.AppenderConfig;
import org.safehaus.penrose.log4j.Log4jConfig;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * @author Endi S. Dewata
 */
public class AppenderNode extends Node {

    Logger log = Logger.getLogger(getClass());

    Server server;
    AppenderConfig appenderConfig;

    public AppenderNode(
            Server server,
            String name,
            Image image,
            Object object,
            Node parent
    ) {
        super(name, image, object, parent);
        this.server = server;
        this.appenderConfig = (AppenderConfig)object;
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(actions.getOpenAction());

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getCopyAction());
        manager.add(actions.getPasteAction());
        manager.add(actions.getDeleteAction());
    }

    public void open() throws Exception {

        Log4jConfig log4jConfig = server.getLog4jConfig();

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        AppenderDialog dialog = new AppenderDialog(shell, SWT.NONE);
        dialog.setText("Edit Appender");
        dialog.setLog4jConfig(log4jConfig);
        dialog.setAppenderConfig(appenderConfig);
        dialog.open();
    }

    public Object copy() throws Exception {
        return appenderConfig;
    }

    public boolean canPaste(Object object) throws Exception {
        return getParent().canPaste(object);
    }

    public void paste(Object object) throws Exception {
        getParent().paste(object);
    }

    public void delete() throws Exception {
        Log4jConfig loggingConfig = server.getLog4jConfig();
        loggingConfig.removeAppenderConfig(appenderConfig.getName());
    }
}
