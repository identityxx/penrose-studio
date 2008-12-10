package org.safehaus.penrose.studio.federation.linking;

import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.table.InvertibleSorter;
import org.safehaus.penrose.federation.IdentityLinkingResult;

/**
 * @author Endi Sukma Dewata
 */
public class StatusSorter extends InvertibleSorter {

    public Logger log = Logger.getLogger(getClass());

    public int compare(Object object1, Object object2) {

        IdentityLinkingResult data1 = (IdentityLinkingResult)object1;
        IdentityLinkingResult data2 = (IdentityLinkingResult)object2;

        String status1 = data1.getStatus();
        String status2 = data2.getStatus();

        //log.debug("Comparing ["+status1+"] with ["+status2+"]");

        if (status1 == null) return -1;
        if (status2 == null) return 1;
        
        return status1.compareTo(status2);
    }
}
