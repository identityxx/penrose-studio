package org.safehaus.penrose.studio.server.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenrosePlugin;
import org.safehaus.penrose.studio.PenroseImage;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.event.SelectionListener;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.event.ChangeListener;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.server.Server;

public class DisconnectAction extends Action implements ChangeListener, SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public DisconnectAction() {
        setText("&Disconnect");
        setImageDescriptor(PenrosePlugin.getImageDescriptor(PenroseImage.DISCONNECT));
        setToolTipText("Disconnect");
        setId(getClass().getName());
    }

    public void run() {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        try {
            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            Node node = penroseStudio.getSelectedNode();
            if (node == null) return;

            ServerNode serverNode = (ServerNode)node;
            serverNode.disconnect();

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    e.getMessage()
            );
        }
    }

    public void updateStatus(Object object) {
        if (object instanceof ServerNode) {
            ServerNode serverNode = (ServerNode)object;
            Server server = serverNode.getServer();
            setEnabled(server.isConnected());

        } else {
            setEnabled(false);
        }
    }

    public void objectChanged(ChangeEvent event) {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Node node = penroseStudio.getSelectedNode();
        updateStatus(node);
    }

    public void objectSelected(SelectionEvent event) {
        Object object = event.getObject();
        updateStatus(object);
    }
}
