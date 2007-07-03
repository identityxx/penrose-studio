package org.safehaus.penrose.studio.nis.action;

import org.safehaus.penrose.ldap.Attributes;

/**
 * @author Endi Sukma Dewata
 */
public class Conflict {

    private Attributes attributes1;
    private Attributes attributes2;

    public Conflict(Attributes attribute1, Attributes attribute2) {
        this.attributes1 = attribute1;
        this.attributes2 = attribute2;
    }

    public Attributes getAttributes1() {
        return attributes1;
    }

    public void setAttributes1(Attributes attributes1) {
        this.attributes1 = attributes1;
    }

    public Attributes getAttributes2() {
        return attributes2;
    }

    public void setAttributes2(Attributes attributes2) {
        this.attributes2 = attributes2;
    }
}
