package org.safehaus.penrose.studio.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.object.ObjectsView;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class NISNode extends Node {

    ObjectsView view;

    public NISNode(ObjectsView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public boolean hasChildren() throws Exception {
        return false;
    }

    public Collection getChildren() throws Exception {
        return null;
    }

    public void open() throws Exception {

        NISEditorInput ei = new NISEditorInput();

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISEditor.class.getName());
    }
}
