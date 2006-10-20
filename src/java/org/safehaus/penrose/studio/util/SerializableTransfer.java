package org.safehaus.penrose.studio.util;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.*;

/**
 * @author Endi S. Dewata
 */
public class SerializableTransfer extends ByteArrayTransfer {

    Logger log = LoggerFactory.getLogger(getClass());

    private static final String SERIALIZABLE = "SERIALIZABLE";
    private static final int SERIALIZABLE_ID = registerType(SERIALIZABLE);

    private static SerializableTransfer instance = new SerializableTransfer();

    private SerializableTransfer() {
    }

    public static SerializableTransfer getInstance() {
        return instance;
    }

    public void javaToNative(Object object, TransferData transferData) {
        if (object == null) {
            log.debug("Object is null");
            return;
        }

        log.debug("Object class: "+object.getClass());
        if (!(object instanceof Serializable)) {
            log.debug("Object not serializable");
            return;
        }

        log.debug("TransferData: "+transferData);
        if (!isSupportedType(transferData)) {
            log.debug("Object type not supported");
            return;
        }

        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);

            oos.writeObject(object);

        } catch (Exception e) {
            log.error(e.getMessage(), e);

        } finally {
            try { if (oos != null) oos.close(); } catch (Exception e) {}
            try { if (baos != null) baos.close(); } catch (Exception e) {}
        }

        super.javaToNative(baos.toByteArray(), transferData);
    }

    public Object nativeToJava(TransferData transferData) {

        log.debug("TransferData: "+transferData);

        if (!isSupportedType(transferData)) {
            log.debug("Object type not supported");
            return null;
        }

        byte[] buffer = (byte[]) super.nativeToJava(transferData);
        if (buffer == null) {
            log.debug("Buffer is null");
            return null;
        }

        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;

        try {
            bais = new ByteArrayInputStream(buffer);
            ois = new ObjectInputStream(bais);

            return (Serializable)ois.readObject();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;

        } finally {
            try { if (ois != null) ois.close(); } catch (Exception e) {}
            try { if (bais != null) bais.close(); } catch (Exception e) {}
        }
    }

    protected String[] getTypeNames() {
        return new String[] { SERIALIZABLE };
    }

    protected int[] getTypeIds() {
        return new int[] { SERIALIZABLE_ID };
    }
}
