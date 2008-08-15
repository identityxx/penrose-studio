package org.safehaus.penrose.studio.nis.action;

import org.apache.log4j.Logger;
import org.safehaus.penrose.federation.NISFederationClient;
import org.safehaus.penrose.studio.project.Project;

/**
 * @author Endi S. Dewata
 */
public class NISAction {

    public Logger log = Logger.getLogger(getClass());

    protected String name;
    protected String description;

    protected Project project;
    protected NISFederationClient nisFederation;

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

    public NISFederationClient getNisFederation() {
        return nisFederation;
    }

    public void setNisFederation(NISFederationClient nisFederation) {
        this.nisFederation = nisFederation;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
