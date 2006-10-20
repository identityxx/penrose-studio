package org.safehaus.penrose.studio.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.event.SelectionListener;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.safehaus.penrose.studio.event.ChangeListener;
import org.safehaus.penrose.studio.project.ProjectsNode;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.util.PenroseStudioClipboard;
import org.safehaus.penrose.studio.tree.Node;

public class PasteAction extends Action implements ChangeListener, SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public PasteAction() {
        setText("&Paste");
        setToolTipText("Paste");
        setAccelerator(SWT.CTRL | 'V');
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
            node.paste(clipboard);

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            MessageDialog.openError(
                    shell,
                    "ERROR",
                    "Failed pasting project.\n"+
                            "See penrose-studio-log.txt for details."
            );
        }
    }

    public void updateStatus(Object object) {
        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        PenroseStudioClipboard clipboard = penroseStudio.getClipboard();

        if (clipboard.isEmpty()) {
            setEnabled(false);

        } else if (object instanceof ProjectsNode) {
            setEnabled(true);

        } else if (object instanceof ProjectNode) {
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
