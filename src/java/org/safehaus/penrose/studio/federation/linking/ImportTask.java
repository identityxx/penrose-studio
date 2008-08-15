package org.safehaus.penrose.studio.federation.linking;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.studio.federation.linking.editor.LinkingPage;
import org.safehaus.penrose.federation.Repository;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author Endi Sukma Dewata
 */
public class ImportTask implements IRunnableWithProgress {

    LinkingPage page;
    Repository repository;

    private List<LocalData> results;

    public ImportTask(LinkingPage page, Repository repository) {
        this.page = page;
        this.repository = repository;
    }

    public void run(final IProgressMonitor monitor) throws InvocationTargetException {
        try {
            monitor.beginTask("Importing "+repository.getName()+"...", results.size());

            while (!results.isEmpty()) {
                if (monitor.isCanceled()) throw new InterruptedException();

                LocalData data = results.remove(0);

                DN dn = data.getDn();

                monitor.subTask("Processing "+dn+"...");

                DN targetDn = page.importEntry(dn);
                data.addLink(targetDn);
                data.removeMatches();

                monitor.worked(1);
            }

        } catch (InterruptedException e) {
            // ignore

        } catch (Exception e) {
            throw new InvocationTargetException(e);

        } finally {
            monitor.done();
        }
    }

    public List<LocalData> getResults() {
        return results;
    }

    public void setResults(List<LocalData> results) {
        this.results = results;
    }
}
