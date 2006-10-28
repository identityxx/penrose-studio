package org.safehaus.penrose.studio.logging;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.action.PenroseStudioActions;
import org.safehaus.penrose.studio.logging.editor.AppenderDialog;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.log4j.Log4jConfig;
import org.safehaus.penrose.log4j.AppenderConfig;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchActionConstants;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Endi S. Dewata
 */
public class AppendersNode extends Node {

    Logger log = Logger.getLogger(getClass());

    Server server;

    public AppendersNode(
            Server server,
            String name,
            Image image,
            Object object,
            Node parent
    ) {
        super(name, image, object, parent);
        this.server = server;
    }

    public void showMenu(IMenuManager manager) {

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioActions actions = penroseStudio.getActions();

        manager.add(new Action("New Appender...") {
            public void run() {
                try {
                    createAppender();
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        manager.add(actions.getPasteAction());
    }

    public void createAppender() throws Exception {

        Log4jConfig loggingConfig = server.getLog4jConfig();

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

    public boolean canPaste(Object object) throws Exception {
        return object instanceof AppenderConfig;
    }

    public void paste(Object object) throws Exception {
        AppenderConfig appenderConfig = (AppenderConfig)object;
        Log4jConfig loggingConfig = server.getLog4jConfig();

        int counter = 1;
        String name = appenderConfig.getName();

        while (loggingConfig.getAppenderConfig(name) != null) {
            counter++;
            name = appenderConfig.getName()+" ("+counter+")";
        }

        appenderConfig.setName(name);
        loggingConfig.addAppenderConfig(appenderConfig);
    }

    public boolean hasChildren() throws Exception {
        Log4jConfig loggingConfig = server.getLog4jConfig();
        return !loggingConfig.getAppenderConfigs().isEmpty();
    }

    public Collection getChildren() throws Exception {

        Collection children = new ArrayList();

        Log4jConfig loggingConfig = server.getLog4jConfig();

        for (Iterator i=loggingConfig.getAppenderConfigs().iterator(); i.hasNext(); ) {
            AppenderConfig appenderConfig = (AppenderConfig)i.next();

            AppenderNode appenderNode = new AppenderNode(
                    server,
                    appenderConfig.getName(),
                    PenrosePlugin.getImage(PenroseImage.APPENDER),
                    appenderConfig,
                    this
            );

            children.add(appenderNode);
        }

        return children;
    }
}
