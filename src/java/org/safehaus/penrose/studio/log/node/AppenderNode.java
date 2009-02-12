package org.safehaus.penrose.studio.log.node;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.log.editor.AppenderEditorInput;
import org.safehaus.penrose.studio.log.editor.AppenderEditor;
import org.safehaus.penrose.log.log4j.AppenderConfig;
import org.safehaus.penrose.log.LogManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Endi S. Dewata
 */
public class AppenderNode extends Node {

    Logger log = Logger.getLogger(getClass());

    ServersView view;
    AppendersNode appendersNode;
    String appenderName;

    public AppenderNode(ServersView view, String appenderName, AppendersNode appendersNode) {
        super(appenderName, PenroseStudio.getImage(PenroseImage.APPENDER), null, appendersNode);

        this.view = view;
        this.appendersNode = appendersNode;
        this.appenderName = appenderName;
    }

    public void showMenu(IMenuManager manager) {

        manager.add(new Action("Open") {
            public void run() {
                try {
                    open();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        manager.add(new Action("Delete", PenroseStudio.getImageDescriptor(PenroseImage.DELETE_SMALL)) {
            public void run() {
                try {
                    remove();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
    }

    public void open() throws Exception {

        AppenderEditorInput ei = new AppenderEditorInput();
        ei.setAppendersNode(appendersNode);
        ei.setAppenderName(appenderName);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, AppenderEditor.class.getName());
/*
        Server server = appendersNode.getLogsNode().getServerNode().getServer();
        PenroseClient client = server.getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();
        AppenderConfig appenderConfig = logManagerClient.getAppenderConfig(appenderName);

        AppenderDialog dialog = new AppenderDialog(view.getSite().getShell(), SWT.NONE);
        dialog.setText("Edit Appender");
        dialog.setAppenderConfig(appenderConfig);
        dialog.open();
*/
    }

    public void remove() throws Exception {

        Shell shell = view.getSite().getShell();

        boolean confirm = MessageDialog.openQuestion(
                shell,
                "Confirmation",
                "Remove Appender \""+appenderName+"\"?");

        if (!confirm) return;

        Server server = appendersNode.getLogsNode().getServerNode().getServer();
        PenroseClient client = server.getClient();
        LogManagerClient logManagerClient = client.getLogManagerClient();

        for (Node node : view.getSelectedNodes()) {
            if (!(node instanceof AppenderNode)) continue;

            AppenderNode appenderNode = (AppenderNode)node;
            logManagerClient.removeAppenderConfig(appenderNode.getAppenderName());
        }

        parent.refresh();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public String getAppenderName() {
        return appenderName;
    }

    public void setAppenderName(String appenderName) {
        this.appenderName = appenderName;
    }
}
