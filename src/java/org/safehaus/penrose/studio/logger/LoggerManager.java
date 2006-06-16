package org.safehaus.penrose.studio.logger;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Endi S. Dewata
 */
public class LoggerManager {

    Logger log = Logger.getLogger(LoggerManager.class);

    Map loggers = new TreeMap();

    public void addLogger(String name) {
        StringTokenizer st = new StringTokenizer(name, ".");
        Map map = loggers;

        //log.debug("Adding logger:");
        while (st.hasMoreTokens()) {
            String rname = st.nextToken();
            //log.debug(" - "+rname);

            Map m = (Map)map.get(rname);
            if (m == null) {
                m = new TreeMap();
                map.put(rname, m);
            }

            map = m;
        }
    }

    public void removeLogger(String name) {
        StringTokenizer st = new StringTokenizer(name, ".");
        Map map = loggers;

        //log.debug("Adding logger:");
        while (st.hasMoreTokens()) {
            String rname = st.nextToken();
            //log.debug(" - "+rname);

            Map m = (Map)map.get(rname);
            if (m == null) {
                m = new TreeMap();
                map.put(rname, m);
            }

            if (!st.hasMoreTokens()) {
                map.remove(rname);
                return;
            }
            
            map = m;
        }
    }

    public Collection getLoggers() {
        return getLoggers(null);
    }

    public Collection getLoggers(String name) {
        if (name == null) {
            return loggers.keySet();
        }

        StringTokenizer st = new StringTokenizer(name, ".");
        Map map = loggers;

        //log.debug("Getting logger:");
        while (st.hasMoreTokens()) {
            String rname = st.nextToken();
            //log.debug(" - "+rname);

            Map m = (Map)map.get(rname);
            if (m == null) {
                m = new TreeMap();
                map.put(rname, m);
            }

            map = m;
        }

        Collection list = new ArrayList();
        for (Iterator i=map.keySet().iterator(); i.hasNext(); ) {
            String rname = (String)i.next();
            list.add(name+"."+rname);
        }

        return list;
    }

    public void clear() {
        loggers.clear();
    }
}
