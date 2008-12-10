package org.safehaus.penrose.studio.federation.linking;

import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.studio.table.InvertibleSorter;
import org.safehaus.penrose.federation.IdentityLinkingResult;
import org.apache.log4j.Logger;

/**
 * @author Endi Sukma Dewata
 */
public class DNSorter extends InvertibleSorter {
    
    public Logger log = Logger.getLogger(getClass());

    public int compare(Object object1, Object object2) {

        IdentityLinkingResult data1 = (IdentityLinkingResult)object1;
        IdentityLinkingResult data2 = (IdentityLinkingResult)object2;

        DN dn1 = data1.getDn();
        DN dn2 = data2.getDn();

        //log.debug("Comparing ["+dn1+"] with ["+dn2+"]");

        return dn1.compareTo(dn2);
    }
}
