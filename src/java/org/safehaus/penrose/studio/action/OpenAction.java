package org.safehaus.penrose.studio.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.safehaus.penrose.studio.event.ChangeListener;
import org.safehaus.penrose.studio.event.SelectionListener;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.tree.Node;
import org.apache.log4j.Logger;

public class OpenAction extends Action implements ChangeListener, SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public OpenAction() {
        setText("&Open");
        setToolTipText("Open");
        setId(getClass().getName());
    }

    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Node node = penroseStudio.getSelectedNode();
        if (node == null) return;

        try {
            node.open();

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            MessageDialog.openError(
                    shell,
                    "Error",
                    "Failed opening "+node.getName()+"."
            );
        }
    }

    public void objectChanged(ChangeEvent event) {
    }

    public void objectSelected(SelectionEvent event) {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Node node = penroseStudio.getSelectedNode();
        if (node == null) {
            setEnabled(false);
        } else {
            setEnabled(true);
        }
    }
}
