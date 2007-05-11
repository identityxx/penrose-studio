package org.safehaus.penrose.studio.nis.action;

import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.adapter.jdbc.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.ldap.SearchRequest;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.ldap.SearchResponse;

import java.util.*;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class ConflictingUIDFinderAction extends NISAction {

    public String sourceName = "cache.users";

    SourceManager sourceManager;
    Map<String,String> map = new TreeMap<String,String>();

    public ConflictingUIDFinderAction() throws Exception {

        setName("Conflicting UID Finder");
        setDescription("Finds users from different domains with conflicting UIDs");

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
        String table1 = catalog1+"."+source1.getParameter(JDBCClient.TABLE);

        String catalog2 = source2.getParameter(JDBCClient.CATALOG);
        String table2 = catalog2+"."+source2.getParameter(JDBCClient.TABLE);

        String sql = "select a.uid, a.uidNumber, b.uidNumber, c.uid, c.uidNumber, d.uidNumber" +
                " from "+table1+" a"+
                " left join nis.users b on b.domain=? and a.uid=b.uid"+
                " join "+table2+" c on a.uid <> c.uid "+
                " left join nis.users d on d.domain=? and c.uid=d.uid"+
                " where b.uidNumber is null and d.uidNumber is null and a.uidNumber = c.uidNumber"+
                    " or b.uidNumber is null and a.uidNumber = d.uidNumber"+
                    " or d.uidNumber is null and b.uidNumber = c.uidNumber"+
                    " or b.uidNumber = d.uidNumber" +
                " order by a.uid";

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(domain1));
        assignments.add(new Assignment(domain2));

        QueryResponse queryResponse = new QueryResponse() {
            public void add(Object object) throws Exception {
                ResultSet rs = (ResultSet)object;

                String uid1 = rs.getString(1);
                Integer origUidNumber1 = (Integer)rs.getObject(2);
                Integer uidNumber1 = (Integer)rs.getObject(3);

                String uid2 = rs.getString(4);
                Integer origUidNumber2 = (Integer)rs.getObject(5);
                Integer uidNumber2 = (Integer)rs.getObject(6);

                Attributes attributes1 = new Attributes();
                attributes1.setValue("domain", domain1);
                attributes1.setValue("partition", partition1);
                attributes1.setValue("uid", uid1);
                attributes1.setValue("origUidNumber", origUidNumber1);
                attributes1.setValue("uidNumber", uidNumber1);

                Attributes attributes2 = new Attributes();
                attributes2.setValue("domain", domain2);
                attributes2.setValue("partition", partition2);
                attributes2.setValue("uid", uid2);
                attributes2.setValue("origUidNumber", origUidNumber2);
                attributes2.setValue("uidNumber", uidNumber2);

                response.add(new Conflict(attributes1, attributes2));
            }
        };

        client1.executeQuery(sql, assignments, queryResponse);
    }
}
