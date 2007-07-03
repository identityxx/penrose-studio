package org.safehaus.penrose.studio.nis.action;

import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class NISAction {

    public Logger log = Logger.getLogger(getClass());

    private String name;
    private String description;

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
}
