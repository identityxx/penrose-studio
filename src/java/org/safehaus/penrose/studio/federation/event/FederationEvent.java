package org.safehaus.penrose.studio.federation.event;

import org.safehaus.penrose.federation.FederationRepositoryConfig;

/**
 * @author Endi Sukma Dewata
 */
public class FederationEvent {
    
    private FederationRepositoryConfig repository;

    public FederationRepositoryConfig getRepository() {
        return repository;
    }

    public void setRepository(FederationRepositoryConfig repository) {
        this.repository = repository;
    }
}
