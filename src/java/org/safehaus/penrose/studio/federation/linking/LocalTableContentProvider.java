package org.safehaus.penrose.studio.federation.linking;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import java.util.Collection;

/**
 * @author Endi Sukma Dewata
 */
public class LocalTableContentProvider implements IStructuredContentProvider {

    public Object[] getElements(Object object) {
        return ((Collection)object).toArray();
    }
    
    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}
