package org.safehaus.penrose.studio.federation.event;

/**
 * @author Endi Sukma Dewata
 */
public interface FederationEventListener {

    public void repositoryAdded(FederationEvent event);
    public void repositoryModified(FederationEvent event);
    public void repositoryRemoved(FederationEvent event);
}
