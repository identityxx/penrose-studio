package org.safehaus.penrose.studio.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.event.SelectionListener;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.safehaus.penrose.studio.event.ChangeListener;
import org.safehaus.penrose.studio.util.PenroseStudioClipboard;
import org.safehaus.penrose.studio.tree.Node;

import java.io.Serializable;

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

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Node node = penroseStudio.getSelectedNode();
        if (node == null) return;

        try {
            Object object = node.copy();
            if (object == null) return;
            if (!(object instanceof Serializable)) return;

            Serializable content = (Serializable)object;
            PenroseStudioClipboard clipboard = penroseStudio.getClipboard();
            clipboard.put(content);

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            MessageDialog.openError(
                    shell,
                    "Error",
                    "Failed copying "+node.getName()+"."
            );
        }
    }

    public void updateStatus(Object object) throws Exception {
        if (object instanceof Node) {
            Node node = (Node)object;
            Object content = node.copy();
            setEnabled(content != null);

        } else {
            setEnabled(false);
        }
    }

    public void objectChanged(ChangeEvent event) {
        try {
            PenroseStudio penroseStudio = PenroseStudio.getInstance();
            Node node = penroseStudio.getSelectedNode();
            updateStatus(node);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void objectSelected(SelectionEvent event) {
        try {
            Object object = event.getObject();
            updateStatus(object);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
