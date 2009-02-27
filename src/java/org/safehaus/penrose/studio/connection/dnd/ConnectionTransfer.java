package org.safehaus.penrose.studio.connection.dnd;

import org.safehaus.penrose.studio.dnd.ObjectTransfer;
import org.safehaus.penrose.connection.ConnectionConfig;

/**
 * @author Endi Sukma Dewata
 */
public class ConnectionTransfer extends ObjectTransfer {

    public final static String TYPE_NAME = "ConnectionConfig";
    public final static int TYPE_ID = registerType(TYPE_NAME);

    public final static ConnectionTransfer INSTANCE = new ConnectionTransfer();

    public static ConnectionTransfer getInstance() {
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
        if (!(object instanceof ConnectionConfig[])) return false;
        if (((ConnectionConfig[])object).length == 0) return false;

        return true;
    }
}