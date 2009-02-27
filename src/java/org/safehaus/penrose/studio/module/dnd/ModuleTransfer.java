package org.safehaus.penrose.studio.module.dnd;

import org.safehaus.penrose.studio.dnd.ObjectTransfer;
import org.safehaus.penrose.module.ModuleConfig;

/**
 * @author Endi Sukma Dewata
 */
public class ModuleTransfer extends ObjectTransfer {

    public final static String TYPE_NAME = "ModuleConfig";
    public final static int TYPE_ID = registerType(TYPE_NAME);

    public final static ModuleTransfer INSTANCE = new ModuleTransfer();

    public static ModuleTransfer getInstance() {
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
        if (!(object instanceof ModuleConfig[])) return false;
        if (((ModuleConfig[])object).length == 0) return false;

        return true;
    }
}