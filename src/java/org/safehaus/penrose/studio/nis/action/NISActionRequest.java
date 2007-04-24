package org.safehaus.penrose.studio.nis.action;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Endi S. Dewata
 */
public class NISActionRequest {

    private List domains = new ArrayList();

    public List getDomains() {
        return domains;
    }

    public void addDomain(String domain) {
        domains.add(domain);
    }
    
    public void setDomains(List domains) {
        if (this.domains == domains) return;
        this.domains.clear();
        this.domains.addAll(domains);
    }
}
