package org.safehaus.penrose.studio.tree;

import org.eclipse.jface.action.IMenuManager;

/**
 * @author Endi S. Dewata
 */
public abstract class TreeProvider {

    public void showMenu(IMenuManager manager, Object object) throws Exception {
    }

    public void open(Object object) throws Exception {
    }
}
