package org.safehaus.penrose.studio.nis.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.studio.nis.NISTool;

/**
 * @author Endi S. Dewata
 */
public class NISEditorInput implements IEditorInput {

    private NISTool nisTool;
    private NISDomain domain;

    public NISEditorInput() {
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "NIS";
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return "NIS";
    }

    public Object getAdapter(Class aClass) {
        return null;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof NISEditorInput)) return false;

        NISEditorInput cei = (NISEditorInput)o;
        if (!domain.getName().equals(cei.domain.getName())) return false;

        return true;
    }

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }

    public NISTool getNisTool() {
        return nisTool;
    }

    public void setNisTool(NISTool nisTool) {
        this.nisTool = nisTool;
    }
}
