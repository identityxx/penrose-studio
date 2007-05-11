package org.safehaus.penrose.studio.nis.action;

import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.SearchRequest;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.ldap.SearchResponse;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.adapter.jdbc.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.QueryResponse;

import java.util.Map;
import java.util.TreeMap;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class ConflictingGIDFinderAction extends NISAction {

    public String sourceName = "cache.groups";

    SourceManager sourceManager;
    Map<String,String> map = new TreeMap<String,String>();

    public ConflictingGIDFinderAction() throws Exception {

        setName("Conflicting GID Finder");
        setDescription("Finds groups from different domains with conflicting GIDs");

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();

        sourceManager = penroseContext.getSourceManager();
        Source domains = sourceManager.getSource("DEFAULT", "penrose.domains");

        SearchRequest searchRequest = new SearchRequest();
        SearchResponse<SearchResult> searchResponse = new SearchResponse<SearchResult>();

        domains.search(searchRequest, searchResponse);

        while (searchResponse.hasNext()) {
            SearchResult searchResult = searchResponse.next();
            Attributes attributes = searchResult.getAttributes();

            String domain = (String)attributes.getValue("name");
            String partition = (String)attributes.getValue("partition");

            map.put(domain, partition);
        }
    }

    public void execute(
            final NISActionRequest request,
            final NISActionResponse response
    ) throws Exception {

        for (String domain : map.keySet()) {
            if (request.getDomain().equals(domain)) continue;
            execute(request.getDomain(), domain, response);
        }

        response.close();
    }

    public void execute(
            final String domain1,
            final String domain2,
            final NISActionResponse response
    ) throws Exception {

        final String partition1 = map.get(domain1);
        final Source source1 = sourceManager.getSource(partition1, sourceName);

        final String partition2 = map.get(domain2);
        final Source source2 = sourceManager.getSource(partition2, sourceName);

        JDBCAdapter adapter1 = (JDBCAdapter)source1.getConnection().getAdapter();
        JDBCClient client1 = adapter1.getClient();

        String catalog1 = source1.getParameter(JDBCClient.CATALOG);
        String table1Name = catalog1+"."+source1.getParameter(JDBCClient.TABLE);

        String catalog2 = source2.getParameter(JDBCClient.CATALOG);
        String table2Name = catalog2+"."+source2.getParameter(JDBCClient.TABLE);

        String sql = "select a.cn, a.gidNumber, b.cn, b.gidNumber" +
                " from "+table1Name+" a, "+table2Name+" b" +
                " where a.cn <> b.cn and a.gidNumber = b.gidNumber" +
                " order by a.cn";

        QueryResponse queryResponse = new QueryResponse() {
            public void add(Object object) throws Exception {
                ResultSet rs = (ResultSet)object;

                String group1 = rs.getString(1);
                Object gidNumber1 = rs.getObject(2);

                String group2 = rs.getString(3);
                Object gidNumber2 = rs.getObject(4);

                Attributes attributes1 = new Attributes();
                attributes1.setValue("domain", domain1);
                attributes1.setValue("partition", partition1);
                attributes1.setValue("source", source1);
                attributes1.setValue("group", group1);
                attributes1.setValue("gidNumber", gidNumber1);

                Attributes attributes2 = new Attributes();
                attributes2.setValue("domain", domain2);
                attributes2.setValue("partition", partition2);
                attributes2.setValue("source", source2);
                attributes2.setValue("group", group2);
                attributes2.setValue("gidNumber", gidNumber2);

                response.add(new Conflict(attributes1, attributes2));
            }
        };

        client1.executeQuery(sql, queryResponse);
    }
}
