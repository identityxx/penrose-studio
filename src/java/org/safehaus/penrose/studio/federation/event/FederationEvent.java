package org.safehaus.penrose.studio.federation.event;

import org.safehaus.penrose.federation.repository.Repository;

/**
 * @author Endi Sukma Dewata
 */
public class FederationEvent {
    
    private Repository repository;

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }
}
