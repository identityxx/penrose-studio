package org.safehaus.penrose.studio.nis.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.nis.NISDomain;

/**
 * @author Endi S. Dewata
 */
public class NISAlignmentToolEditorInput implements IEditorInput {

    private NISTool nisTool;
    private NISDomain domain;

    public NISAlignmentToolEditorInput() {
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "NIS Users - "+domain.getName();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return "NIS Users - "+domain.getName();
    }

    public Object getAdapter(Class aClass) {
        return null;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof NISAlignmentToolEditorInput)) return false;

        NISAlignmentToolEditorInput cei = (NISAlignmentToolEditorInput)o;
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
