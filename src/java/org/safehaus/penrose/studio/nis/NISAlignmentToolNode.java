package org.safehaus.penrose.studio.nis;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.nis.editor.NISAlignmentToolEditorInput;
import org.safehaus.penrose.studio.nis.editor.NISAlignmentToolEditor;
import org.safehaus.penrose.nis.NISDomain;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISAlignmentToolNode extends Node {

    ServersView view;

    private NISTool nisTool;
    private NISDomain domain;

    public NISAlignmentToolNode(ServersView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void open() throws Exception {

        NISAlignmentToolEditorInput ei = new NISAlignmentToolEditorInput();
        ei.setNisTool(nisTool);
        ei.setDomain(domain);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISAlignmentToolEditor.class.getName());
    }

    public boolean hasChildren() throws Exception {
        return false;
    }

    public Collection<Node> getChildren() throws Exception {
        return new ArrayList<Node>();
    }

    public NISTool getNisTool() {
        return nisTool;
    }

    public void setNisTool(NISTool nisTool) {
        this.nisTool = nisTool;
    }

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }
}
