package org.safehaus.penrose.studio.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.safehaus.penrose.studio.event.SelectionListener;
import org.safehaus.penrose.studio.event.SelectionEvent;
import org.safehaus.penrose.studio.event.ChangeEvent;
import org.safehaus.penrose.studio.event.ChangeListener;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.studio.project.ProjectEditorDialog;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
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

        try {
            IWorkbenchPage page = window.getActivePage();
            ObjectsView objectsView = (ObjectsView)page.showView(ObjectsView.class.getName());

            ProjectNode projectNode = objectsView.getSelectedProjectNode();
            if (projectNode == null) return;

            Project project = projectNode.getProject();

            String oldProjectName = project.getName();
            System.out.println("Editing project: "+oldProjectName);

            ProjectEditorDialog dialog = new ProjectEditorDialog(shell, SWT.NONE);
            dialog.setProjectConfig(project.getProjectConfig());
            dialog.open();

            if (dialog.getAction() == ProjectEditorDialog.CANCEL) return;

            PenroseStudio penroseStudio = PenroseStudio.getInstance();

            if (!oldProjectName.equals(project.getName())) {
                penroseStudio.removeProject(oldProjectName);
                penroseStudio.addProject(project.getProjectConfig());
            }

            penroseStudio.save();

            log.debug("Project updated.");

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
        if (object instanceof ProjectNode) {
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