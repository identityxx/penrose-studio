package org.safehaus.penrose.studio.table;

import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Endi Sukma Dewata
 */
public abstract class InvertableSorter extends ViewerSorter {

    public abstract int compare(Viewer viewer, Object e1, Object e2);
	public abstract InvertableSorter getInverseSorter();
	public abstract int getSortDirection();
}
