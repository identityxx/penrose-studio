package org.safehaus.penrose.studio.federation.linking;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.safehaus.penrose.federation.Repository;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.studio.federation.linking.editor.LinkingPage;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * @author Endi Sukma Dewata
 */
public class DeleteTask implements IRunnableWithProgress {

    LinkingPage page;
    Repository repository;

    private LocalData data;

    private String baseDn;
    private String filter;
    private int scope;

    private Collection<DN> dns;
    
    public DeleteTask(LinkingPage page, Repository repository) {
        this.page = page;
        this.repository = repository;
    }

    public void run(final IProgressMonitor monitor) throws InvocationTargetException {
        try {
            monitor.beginTask("Deleting "+repository.getName()+"...", dns.size());

            for (DN dn : dns) {
                if (monitor.isCanceled()) throw new InterruptedException();

                monitor.subTask("Deleting "+dn+"...");

                page.deleteEntry(dn);

                data.removeLink(dn);
                data.removeMatch(dn);

                monitor.worked(1);
            }

            if (!data.hasLinks()) {
                Filter f = page.createFilter(data.getEntry(), filter);
                Collection<DN> matches = page.searchMatches(baseDn, f, scope);
                data.setMatches(matches);
            }

        } catch (InterruptedException e) {
            // ignore

        } catch (Exception e) {
            throw new InvocationTargetException(e);

        } finally {
            monitor.done();
        }
    }

    public LocalData getData() {
        return data;
    }

    public void setData(LocalData data) {
        this.data = data;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public Collection<DN> getDns() {
        return dns;
    }

    public void setDns(Collection<DN> dns) {
        this.dns = dns;
    }
}
