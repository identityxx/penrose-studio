package org.safehaus.penrose.studio.federation.nis.ownership;

import org.safehaus.penrose.studio.tree.Node;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.nis.NISRepository;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class NISFilesNode extends Node {

    private NISFederation nisFederation;
    private NISRepository domain;

    public NISFilesNode(String name, String type, Image image, Object object, Object parent) {
        super(name, type, image, object, parent);
    }

    public void open() throws Exception {

        NISFilesEditorInput ei = new NISFilesEditorInput();
        ei.setNisTool(nisFederation);
        ei.setDomain(domain);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        page.openEditor(ei, NISFilesEditor.class.getName());
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

    public NISRepository getDomain() {
        return domain;
    }

    public void setDomain(NISRepository domain) {
        this.domain = domain;
    }
}
