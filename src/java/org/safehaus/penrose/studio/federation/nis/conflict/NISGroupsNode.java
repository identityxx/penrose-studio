package org.safehaus.penrose.studio.federation.nis.conflict;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.federation.repository.NISDomain;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IWorkbenchWindow;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISGroupsNode extends Node {

    private NISFederation nisFederation;
    private NISDomain domain;

    public NISGroupsNode(String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
    }

    public void open() throws Exception {

        NISGroupsEditorInput ei = new NISGroupsEditorInput();
        ei.setNisTool(nisFederation);
        ei.setDomain(domain);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISGroupsEditor.class.getName());
    }

    public boolean hasChildren() throws Exception {
        return false;
    }

    public Collection<Node> getChildren() throws Exception {
        return new ArrayList<Node>();
    }

    public NISFederation getNisTool() {
        return nisFederation;
    }

    public void setNisTool(NISFederation nisFederation) {
        this.nisFederation = nisFederation;
    }

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }
}
