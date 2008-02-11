package org.safehaus.penrose.studio.federation.event;

import org.safehaus.penrose.studio.federation.RepositoryConfig;

/**
 * @author Endi Sukma Dewata
 */
public class FederationEvent {
    
    private RepositoryConfig repository;

    public RepositoryConfig getRepository() {
        return repository;
    }

    public void setRepository(RepositoryConfig repository) {
        this.repository = repository;
    }
}
