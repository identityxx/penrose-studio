package org.safehaus.penrose.studio.util;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * @author Endi S. Dewata
 */
public class PenroseStudioClipboard {

    Logger log = LoggerFactory.getLogger(getClass());

    Clipboard clipboard;

    public PenroseStudioClipboard(Display display) {
        clipboard = new Clipboard(display);
    }

    public void put(Serializable object) {
        put(new Serializable[] { object });
    }

    public void put(Serializable[] objects) {
        clipboard.setContents(
                objects,
                new Transfer[] { SerializableTransfer.getInstance() }
        );
    }

    public Serializable get() {
        Serializable object = (Serializable)clipboard.getContents(
                SerializableTransfer.getInstance()
        );

        return object;
    }

    public boolean isEmpty() {
        return get() == null;
    }
}
