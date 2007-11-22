package org.safehaus.penrose.studio.table;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;

/**
 * @author Endi Sukma Dewata
 */
public abstract class InvertibleSorter extends ViewerSorter {

    public int direction = SWT.UP;

    public int compare(Viewer viewer, Object object1, Object object2) {
        if (direction == SWT.UP) {
            return compare(object1, object2);
        } else {
            return -compare(object1, object2);
        }
    }

    public abstract int compare(Object object1, Object object2);

	public int getDirection() {
		return direction;
	}

    public void reset() {
        direction = SWT.UP;
    }

    public void invert() {
        direction = direction == SWT.UP ? SWT.DOWN : SWT.UP;
    }
}