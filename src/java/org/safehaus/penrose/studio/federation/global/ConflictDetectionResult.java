package org.safehaus.penrose.studio.federation.global;

import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.ldap.DN;

import java.util.Collection;
import java.util.TreeMap;
import java.util.Map;

/**
 * @author Endi Sukma Dewata
 */
public class ConflictDetectionResult {

    SearchResult entry;

    Map<DN,SearchResult> conflicts = new TreeMap<DN,SearchResult>();

    public ConflictDetectionResult(SearchResult entry) {
        this.entry = entry;
    }

    public SearchResult getEntry() {
        return entry;
    }

    public void setEntry(SearchResult entry) {
        this.entry = entry;
    }

    public void addConflict(SearchResult conflict) {
        conflicts.put(conflict.getDn(), conflict);
    }

    public SearchResult removeConflict(DN dn) {
        return conflicts.remove(dn);
    }

    public Collection<SearchResult> getConflicts() {
        return conflicts.values();
    }

    public void removeConflicts() {
        conflicts.clear();
    }
}
