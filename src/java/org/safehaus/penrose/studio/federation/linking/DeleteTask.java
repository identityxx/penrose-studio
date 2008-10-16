package org.safehaus.penrose.studio.federation.linking;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.federation.LinkingData;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.studio.federation.linking.editor.LinkingPage;

import javax.management.MBeanException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * @author Endi Sukma Dewata
 */
public class DeleteTask implements IRunnableWithProgress {

    LinkingPage page;
    FederationRepositoryConfig repository;

    private LinkingData data;

    private String baseDn;
    private String filter;
    private int scope;

    private Collection<SearchResult> globalEntries;
    
    public DeleteTask(LinkingPage page, FederationRepositoryConfig repository) {
        this.page = page;
        this.repository = repository;
    }

    public void run(final IProgressMonitor monitor) throws InvocationTargetException {
        try {
            monitor.beginTask("Deleting "+repository.getName()+"...", globalEntries.size());

            for (SearchResult globalEntry : globalEntries) {
                if (monitor.isCanceled()) throw new InterruptedException();

                monitor.subTask("Deleting "+globalEntry+"...");

                DN localDn = data.getDn();
                page.linkingClient.deleteEntry(localDn, globalEntry.getDn());

                data.setSearched(false);
                data.removeLinkedEntry(globalEntry.getDn());
                data.removeMatchedEntry(globalEntry.getDn());
                page.loadLocalEntry(data);
                page.updateStatus(data);

                monitor.worked(1);
            }
/*
            if (!data.hasLinks()) {
                Filter f = page.createFilter(data.getEntry(), filter);
                Collection<DN> matches = page.searchMatches(baseDn, f, scope);
                data.setMatches(matches);
            }
*/
        } catch (InterruptedException e) {
            // ignore

        } catch (MBeanException e) {
            throw new InvocationTargetException(e.getCause());

        } catch (Exception e) {
            throw new InvocationTargetException(e);

        } finally {
            monitor.done();
        }
    }

    public LinkingData getData() {
        return data;
    }

    public void setData(LinkingData data) {
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

    public Collection<SearchResult> getGlobalEntries() {
        return globalEntries;
    }

    public void setGlobalEntries(Collection<SearchResult> globalEntries) {
        this.globalEntries = globalEntries;
    }
}
