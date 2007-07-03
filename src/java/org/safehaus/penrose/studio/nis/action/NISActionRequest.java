package org.safehaus.penrose.studio.nis.action;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Endi S. Dewata
 */
public class NISActionRequest {

    private String domain;
    private List<String> domains = new ArrayList<String>();

    public List<String> getDomains() {
        return domains;
    }

    public void addDomain(String domain) {
        domains.add(domain);
    }
    
    public void setDomains(List<String> domains) {
        if (this.domains == domains) return;
        this.domains.clear();
        this.domains.addAll(domains);
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
