package org.safehaus.penrose.studio.mapping.dnd;

import org.safehaus.penrose.studio.dnd.ObjectTransfer;
import org.safehaus.penrose.mapping.MappingConfig;

/**
 * @author Endi Sukma Dewata
 */
public class MappingTransfer extends ObjectTransfer {

    public final static String TYPE_NAME = "MappingConfig";
    public final static int TYPE_ID = registerType(TYPE_NAME);

    public final static MappingTransfer INSTANCE = new MappingTransfer();

    public static MappingTransfer getInstance() {
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
        if (!(object instanceof MappingConfig[])) return false;
        if (((MappingConfig[])object).length == 0) return false;

        return true;
    }
}