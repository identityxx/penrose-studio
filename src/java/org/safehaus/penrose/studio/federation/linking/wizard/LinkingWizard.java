package org.safehaus.penrose.studio.federation.linking.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.apache.log4j.Logger;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.studio.federation.linking.wizard.LinkingResultsPage;
import org.safehaus.penrose.studio.federation.linking.wizard.LinkingSearchPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

import java.util.Collection;

/**
 * @author Endi Sukma Dewata
 */
public class LinkingWizard extends Wizard {

    Logger log = Logger.getLogger(getClass());

    private DN dn;
    private Filter filter;
    private SearchResult searchResult;
    private PartitionClient partitionClient;
    private Collection<SearchResult> results;

    LinkingSearchPage searchPage;
    LinkingResultsPage resultsPage;

    public LinkingWizard() {
        setWindowTitle("Linking Wizard");
    }

    public void addPages() {
        searchPage = new LinkingSearchPage();
        searchPage.setDn(dn);
        searchPage.setFilter(filter);
        searchPage.setSearchResult(searchResult);
        searchPage.setPartitionClient(partitionClient);
        addPage(searchPage);

        resultsPage = new LinkingResultsPage();
        resultsPage.setDn(dn);
        resultsPage.setSearchResult(searchResult);
        resultsPage.setPartitionClient(partitionClient);
        addPage(resultsPage);
    }

    public IWizardPage getNextPage(IWizardPage page) {
        if (page == searchPage) {

            DN searchBaseDn = new DN(searchPage.getBaseDn());
            String filter = searchPage.getFilter();
            int scope = searchPage.getScope();

            Collection<SearchResult> results = search(searchBaseDn, filter, scope);

            resultsPage.setResults(results);

            return resultsPage;

        } else {
            return super.getNextPage(page);
        }
    }

    public Collection<SearchResult> search(DN baseDn, String filter, int scope) {
        try {

            SearchRequest request = new SearchRequest();
            request.setDn(baseDn);
            request.setFilter(filter);
            request.setScope(scope);

            SearchResponse response = new SearchResponse();

            partitionClient.search(request, response);

            return response.getResults();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }

        return null;
    }

    public boolean performFinish() {
        try {
            results = resultsPage.getSelections();

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
            return false;
        }
    }

    public PartitionClient getPartitionClient() {
        return partitionClient;
    }

    public void setPartitionClient(PartitionClient partitionClient) {
        this.partitionClient = partitionClient;
    }

    public SearchResult getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(SearchResult searchResult) {
        this.searchResult = searchResult;
    }

    public DN getDn() {
        return dn;
    }

    public void setDn(DN dn) {
        this.dn = dn;
    }

    public Collection<SearchResult> getResults() {
        return results;
    }

    public void setResults(Collection<SearchResult> results) {
        this.results = results;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}
