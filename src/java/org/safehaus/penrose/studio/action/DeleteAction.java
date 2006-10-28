package org.safehaus.penrose.studio.action;

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
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.event.SelectionListener;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.safehaus.penrose.studio.event.ChangeListener;

import java.util.Collection;
import java.util.Iterator;

public class DeleteAction extends Action implements ChangeListener, SelectionListener {

    Logger log = Logger.getLogger(getClass());

    public DeleteAction() {
        setText("&Delete");
        setImageDescriptor(PenrosePlugin.getImageDescriptor(PenroseImage.DELETE));
        setToolTipText("Delete");
        setId(getClass().getName());
    }

    public void run() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Shell shell = window.getShell();

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        Collection nodes = penroseStudio.getSelectedNodes();

        for (Iterator i=nodes.iterator(); i.hasNext(); ) {
            Node node = (Node)i.next();

            try {
                boolean confirm = MessageDialog.openQuestion(
                        shell,
                        "Confirmation",
                        "Remove "+node.getName()+"?"
                );

                if (!confirm) continue;

                node.delete();

            } catch (Exception e) {
                log.error(e.getMessage(), e);

                MessageDialog.openError(
                        shell,
                        "Error",
                        "Failed deleting "+node.getName()+"."
                );
            }
        }

        penroseStudio.fireChangeEvent();
    }

    public void objectChanged(ChangeEvent event) {
    }

    public void objectSelected(SelectionEvent event) {
    }
}
