package org.safehaus.penrose.studio.nis.conflict;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.server.ServersView;
import org.safehaus.penrose.studio.nis.conflict.NISUsersEditorInput;
import org.safehaus.penrose.studio.nis.conflict.NISUsersEditor;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.nis.NISDomain;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchPage;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISUsersNode extends Node {

    ServersView view;

    private NISTool nisTool;
    private NISDomain domain;

    public NISUsersNode(ServersView view, String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
        this.view = view;
    }

    public void open() throws Exception {

        NISUsersEditorInput ei = new NISUsersEditorInput();
        ei.setNisTool(nisTool);
        ei.setDomain(domain);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISUsersEditor.class.getName());
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
