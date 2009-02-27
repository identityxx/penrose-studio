package org.safehaus.penrose.studio.directory.dnd;

import org.safehaus.penrose.studio.dnd.ObjectTransfer;
import org.safehaus.penrose.directory.EntryConfig;

/**
 * @author Endi Sukma Dewata
 */
public class EntryTransfer extends ObjectTransfer {

    public final static String TYPE_NAME = "EntryConfig";
    public final static int TYPE_ID = registerType(TYPE_NAME);

    public final static EntryTransfer INSTANCE = new EntryTransfer();

    public static EntryTransfer getInstance() {
        return INSTANCE;
    }

    protected int[] getTypeIds() {
        return new int[] { TYPE_ID };
    }

    protected String[] getTypeNames() {
        return new String[] { TYPE_NAME };
    }

    public boolean validate(Object object) {
        if (!super.validate(object)) return false;
        if (!(object instanceof EntryConfig[])) return false;
        if (((EntryConfig[])object).length == 0) return false;

        return true;
    }
}