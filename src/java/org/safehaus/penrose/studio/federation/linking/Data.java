package org.safehaus.penrose.studio.federation.linking;

import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.ldap.DN;

import java.util.Collection;
import java.util.ArrayList;

/**
 * @author Endi Sukma Dewata
 */
public class Data {

    private SearchResult entry;
    private boolean searched;

    private Collection<DN> links = new ArrayList<DN>();
    private Collection<DN> matches = new ArrayList<DN>();

    public DN getDn() {
        return entry.getDn();
    }
    
    public SearchResult getEntry() {
        return entry;
    }

    public void setEntry(SearchResult entry) {
        this.entry = entry;
    }

    public DN getLink() {
        return links.isEmpty() ? null : links.iterator().next();
    }

    public Collection<DN> getLinks() {
        return links;
    }

    public void addLink(DN dn) {
        links.add(dn);
    }

    public void addLinks(Collection<DN> links) {
        if (links == null) return;
        this.links.addAll(links);
    }

    public void setLinks(Collection<DN> links) {
        if (this.links == links) return;
        this.links.clear();
        if (links == null) return;
        this.links.addAll(links);
    }

    public void removeLinks() {
        links.clear();
    }

    public Collection<DN> getMatches() {
        return matches;
    }

    public void addMatch(DN dn) {
        matches.add(dn);
    }

    public void addMatches(Collection<DN> matches) {
        if (matches == null) return;
        this.matches.addAll(matches);
    }

    public void setMatches(Collection<DN> matches) {
        if (this.matches == matches) return;
        this.matches.clear();
        if (matches == null) return;
        this.matches.addAll(matches);
    }

    public void removeMatches() {
        matches.clear();
    }

    public boolean isSearched() {
        return searched;
    }

    public void setSearched(boolean searched) {
        this.searched = searched;
    }
}
