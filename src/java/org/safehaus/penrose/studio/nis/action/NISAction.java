package org.safehaus.penrose.studio.nis.action;

import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.nis.NISTool;

/**
 * @author Endi S. Dewata
 */
public class NISAction {

    public Logger log = Logger.getLogger(getClass());

    protected String name;
    protected String description;
    protected NISTool nisTool;

    public void execute(NISActionRequest request, NISActionResponse response) throws Exception {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NISTool getNisTool() {
        return nisTool;
    }

    public void setNisTool(NISTool nisTool) {
        this.nisTool = nisTool;
    }
}
