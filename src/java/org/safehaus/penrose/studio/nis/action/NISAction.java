package org.safehaus.penrose.studio.nis.action;

import org.apache.log4j.Logger;
import org.safehaus.penrose.studio.federation.nis.NISFederation;

/**
 * @author Endi S. Dewata
 */
public class NISAction {

    public Logger log = Logger.getLogger(getClass());

    protected String name;
    protected String description;
    protected NISFederation nisFederation;

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

    public NISFederation getNisTool() {
        return nisFederation;
    }

    public void setNisTool(NISFederation nisFederation) {
        this.nisFederation = nisFederation;
    }
}
