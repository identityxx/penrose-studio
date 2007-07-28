package org.safehaus.penrose.studio.nis.action;

import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.SearchRequest;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.ldap.SearchResponse;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.Partitions;

import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.util.ArrayList;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class InconsistentGIDFinderAction extends NISAction {

    public String sourceName = "cache.groups";

    Partitions partitions;
    Map<String,String> map = new TreeMap<String,String>();

    public InconsistentGIDFinderAction() throws Exception {

        setName("Inconsistent GID Finder");
        setDescription("Finds groups with inconsistent GID numbers across domains");

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        partitions = penroseApplication.getPartitions();
        Partition partition = partitions.getPartition("DEFAULT");

        Source domains = partition.getSource("penrose.domains");

        SearchRequest searchRequest = new SearchRequest();
        SearchResponse<SearchResult> searchResponse = new SearchResponse<SearchResult>();

        domains.search(searchRequest, searchResponse);

        while (searchResponse.hasNext()) {
            SearchResult searchResult = searchResponse.next();
            Attributes attributes = searchResult.getAttributes();

            String domain = (String)attributes.getValue("name");
            String partitionName = (String)attributes.getValue("partition");

            map.put(domain, partitionName);
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

        final String partitionName1 = map.get(domain1);
        Partition partition1 = partitions.getPartition(partitionName1);
        final Source source1 = partition1.getSource(sourceName);

        final String partitionName2 = map.get(domain2);
        Partition partition2 = partitions.getPartition(partitionName2);
        final Source source2 = partition2.getSource(sourceName);

        JDBCAdapter adapter1 = (JDBCAdapter)source1.getConnection().getAdapter();
        JDBCClient client1 = adapter1.getClient();

        String catalog1 = source1.getParameter(JDBCClient.CATALOG);
        String table1 = catalog1+"."+source1.getParameter(JDBCClient.TABLE);

        String catalog2 = source2.getParameter(JDBCClient.CATALOG);
        String table2 = catalog2+"."+source2.getParameter(JDBCClient.TABLE);

        String sql = "select a.cn, a.gidNumber, b.gidNumber, c.cn, c.gidNumber, d.gidNumber" +
                " from "+table1+" a"+
                " left join nis.groups b on b.domain=? and a.cn=b.cn"+
                " join "+table2+" c on a.cn = c.cn "+
                " left join nis.groups d on d.domain=? and c.cn=d.cn"+
                " where b.gidNumber is null and d.gidNumber is null and a.gidNumber <> c.gidNumber"+
                    " or b.gidNumber is null and a.gidNumber <> d.gidNumber"+
                    " or d.gidNumber is null and b.gidNumber <> c.gidNumber"+
                    " or b.gidNumber <> d.gidNumber" +
                " order by a.cn";

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(domain1));
        assignments.add(new Assignment(domain2));

        QueryResponse queryResponse = new QueryResponse() {
            public void add(Object object) throws Exception {
                ResultSet rs = (ResultSet)object;

                String cn1 = rs.getString(1);
                Integer origGidNumber1 = (Integer)rs.getObject(2);
                Integer gidNumber1 = (Integer)rs.getObject(3);

                String cn2 = rs.getString(4);
                Integer origGidNumber2 = (Integer)rs.getObject(5);
                Integer gidNumber2 = (Integer)rs.getObject(6);

                Attributes attributes1 = new Attributes();
                attributes1.setValue("domain", domain1);
                attributes1.setValue("partition", partitionName1);
                attributes1.setValue("cn", cn1);
                attributes1.setValue("origGidNumber", origGidNumber1);
                attributes1.setValue("gidNumber", gidNumber1);

                Attributes attributes2 = new Attributes();
                attributes2.setValue("domain", domain2);
                attributes2.setValue("partition", partitionName2);
                attributes2.setValue("cn", cn2);
                attributes2.setValue("origGidNumber", origGidNumber2);
                attributes2.setValue("gidNumber", gidNumber2);

                response.add(new Conflict(attributes1, attributes2));
            }
        };

        client1.executeQuery(sql, assignments, queryResponse);
    }
}
