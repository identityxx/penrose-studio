package org.safehaus.penrose.studio.source.dnd;

import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.dnd.ObjectTransfer;

/**
 * @author Endi Sukma Dewata
 */
public class SourceTransfer extends ObjectTransfer {

    public final static String TYPE_NAME = "SourceConfig";
    public final static int TYPE_ID = registerType(TYPE_NAME);

    public final static SourceTransfer INSTANCE = new SourceTransfer();

    public static SourceTransfer getInstance() {
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
        if (!(object instanceof SourceConfig[])) return false;
        if (((SourceConfig[])object).length == 0) return false;

        return true;
    }
}
