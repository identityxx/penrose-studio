package org.safehaus.penrose.studio.util;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.MenuManager;

/**
 * @author Endi S. Dewata
 */
public class SWTUtil {

    public static void hookContextMenu(Control control, IMenuListener menuListener) {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(menuListener);
        Menu menu = menuMgr.createContextMenu(control);
        control.setMenu(menu);
    }
}
