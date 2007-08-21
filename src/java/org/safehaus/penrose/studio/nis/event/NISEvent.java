package org.safehaus.penrose.studio.nis.event;

import org.safehaus.penrose.nis.NISDomain;

/**
 * @author Endi Sukma Dewata
 */
public class NISEvent {
    
    private NISDomain domain;

    public NISDomain getDomain() {
        return domain;
    }

    public void setDomain(NISDomain domain) {
        this.domain = domain;
    }
}
