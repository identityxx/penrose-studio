package org.safehaus.penrose.studio.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.server.ServerNode;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.event.SelectionListener;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.safehaus.penrose.studio.event.ChangeListener;
import org.safehaus.penrose.studio.util.PenroseStudioClipboard;
import org.safehaus.penrose.studio.tree.Node;

public class CopyAction extends Action implements ChangeListener, SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public CopyAction() {
        setText("&Copy");
        setToolTipText("Copy");
        setAccelerator(SWT.CTRL | 'C');
        setId(getClass().getName());
    }

    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        try {
            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            Node node = penroseStudio.getSelectedNode();
            if (node == null) return;

            PenroseStudioClipboard clipboard = penroseStudio.getClipboard();
            node.copy(clipboard);
            penroseStudio.fireChangeEvent();

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    "Failed copying project.\n"+
                            "See penrose-studio-log.txt for details."
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
