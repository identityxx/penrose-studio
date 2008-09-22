package org.safehaus.penrose.studio.federation.linking;

import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.studio.table.InvertibleSorter;
import org.safehaus.penrose.federation.LinkingData;
import org.apache.log4j.Logger;

/**
 * @author Endi Sukma Dewata
 */
public class DNSorter extends InvertibleSorter {
    
    public Logger log = Logger.getLogger(getClass());

    public int compare(Object object1, Object object2) {

        LinkingData data1 = (LinkingData)object1;
        LinkingData data2 = (LinkingData)object2;

        DN dn1 = data1.getDn();
        DN dn2 = data2.getDn();

        //log.debug("Comparing ["+dn1+"] with ["+dn2+"]");

        return dn1.compareTo(dn2);
    }
}
