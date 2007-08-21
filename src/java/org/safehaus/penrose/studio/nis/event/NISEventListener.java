package org.safehaus.penrose.studio.nis.event;

/**
 * @author Endi Sukma Dewata
 */
public interface NISEventListener {

    public void domainAdded(NISEvent event);
    public void domainModified(NISEvent event);
    public void domainRemoved(NISEvent event);
}
