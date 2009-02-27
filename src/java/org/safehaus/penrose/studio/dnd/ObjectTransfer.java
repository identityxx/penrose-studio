package org.safehaus.penrose.studio.dnd;

import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author Endi Sukma Dewata
 */
public abstract class ObjectTransfer extends ByteArrayTransfer {

    public Logger log = LoggerFactory.getLogger(getClass());

    protected void javaToNative(Object object, TransferData transferData) {
        if (!validate(object) || !isSupportedType(transferData)) {
            DND.error(DND.ERROR_INVALID_DATA);
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(object);
            os.close();

            byte[] buffer = out.toByteArray();

            super.javaToNative(buffer, transferData);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    protected Object nativeToJava(TransferData transferData) {
        if (!isSupportedType(transferData)) return null;

        byte[] buffer = (byte[])super.nativeToJava(transferData);
        if (buffer == null) return null;

        try {
            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            ObjectInputStream is = new ObjectInputStream(in);
            Object object = is.readObject();
            is.close();

            return object;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public boolean validate(Object object) {
        if (object == null) return false;
        if (!(object instanceof Serializable)) return false;

        return super.validate(object);
    }
}