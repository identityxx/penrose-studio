package org.safehaus.penrose.studio.federation.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Endi S. Dewata
 */
public class TestEditorInput implements IEditorInput {

    public TestEditorInput() {
    }

    public boolean exists() {
        return true;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return "Test";
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return "Test";
    }

    public Object getAdapter(Class aClass) {
        return null;
    }
}
