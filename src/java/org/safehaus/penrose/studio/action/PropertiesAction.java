package org.safehaus.penrose.studio.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.safehaus.penrose.studio.event.SelectionListener;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.safehaus.penrose.studio.event.ChangeListener;
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.studio.server.editor.ServerEditorInput;
import org.safehaus.penrose.studio.server.editor.ServerEditor;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.tree.Node;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class PropertiesAction extends Action implements ChangeListener, SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public PropertiesAction() {
        setText("&Properties");
        setToolTipText("Properties");
        setId(getClass().getName());
    }

    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Node node = penroseStudio.getSelectedNode();
        if (node == null) return;

        try {
            ServerNode serverNode = (ServerNode)node;
            Server server = serverNode.getServer();

            ServerEditorInput ei = new ServerEditorInput();
            ei.setServer(server);

            IWorkbenchPage page = window.getActivePage();
            page.openEditor(ei, ServerEditor.class.getName());

        } catch (Exception e) {
            log.debug(e.getMessage(), e);

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    e.getMessage()
            );
        }
    }

    public void updateStatus(Object object) {
        if (object instanceof ServerNode) {
            setEnabled(true);
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