package org.safehaus.penrose.studio.federation.linking;

import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.table.InvertibleSorter;

/**
 * @author Endi Sukma Dewata
 */
public class StatusSorter extends InvertibleSorter {

    public Logger log = Logger.getLogger(getClass());

    public int compare(Object object1, Object object2) {

        LocalData data1 = (LocalData)object1;
        LocalData data2 = (LocalData)object2;

        String status1 = data1.getStatus();
        String status2 = data2.getStatus();

        //log.debug("Comparing ["+status1+"] with ["+status2+"]");
        
        return status1.compareTo(status2);
    }
}
