package org.safehaus.penrose.studio.nis.action;

import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.nis.NISDomain;

import java.util.Collection;
import java.util.ArrayList;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class ConflictingGIDFinderAction extends NISAction {

    public final static String CACHE_GROUPS = "cache_groups";

    public ConflictingGIDFinderAction() throws Exception {
        setName("Conflicting GID Finder");
        setDescription("Finds groups from different domains with conflicting GIDs");
    }

    public void execute(
            final NISActionRequest request,
            final NISActionResponse response
    ) throws Exception {

        String domainName1 = request.getDomain();
        NISDomain domain1 = nisTool.getNisDomains().get(domainName1);

        for (String domainName2 : nisTool.getNisDomains().keySet()) {
            if (domainName1.equals(domainName2)) continue;

            NISDomain domain2 = nisTool.getNisDomains().get(domainName2);
            execute(domain1, domain2, response);
        }

        response.close();
    }

    public void execute(
            final NISDomain domain1,
            final NISDomain domain2,
            final NISActionResponse response
    ) throws Exception {

        log.debug("Checking conflicts between "+domain1.getName()+" and "+domain2.getName()+".");

        final Partition partition1 = nisTool.getPartitions().getPartition(domain1.getPartition());
        final Source source1 = partition1.getSource(CACHE_GROUPS);

        final Partition partition2 = nisTool.getPartitions().getPartition(domain2.getPartition());
        final Source source2 = partition2.getSource(CACHE_GROUPS);

        JDBCAdapter adapter1 = (JDBCAdapter)source1.getConnection().getAdapter();
        JDBCClient client1 = adapter1.getClient();

        String catalog1 = source1.getParameter(JDBCClient.CATALOG);
        String table1 = catalog1+"."+source1.getParameter(JDBCClient.TABLE);

        String catalog2 = source2.getParameter(JDBCClient.CATALOG);
        String table2 = catalog2+"."+source2.getParameter(JDBCClient.TABLE);

        String sql = "select a.cn, a.gidNumber, b.gidNumber, c.cn, c.gidNumber, d.gidNumber" +
                " from "+table1+" a"+
                " left join nis.groups b on b.domain=? and a.cn=b.cn"+
                " join "+table2+" c on a.cn <> c.cn "+
                " left join nis.groups d on d.domain=? and c.cn=d.cn"+
                " where b.gidNumber is null and d.gidNumber is null and a.gidNumber = c.gidNumber"+
                    " or b.gidNumber is null and a.gidNumber = d.gidNumber"+
                    " or d.gidNumber is null and b.gidNumber = c.gidNumber"+
                    " or b.gidNumber = d.gidNumber" +
                " order by a.cn";

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(domain1.getName()));
        assignments.add(new Assignment(domain2.getName()));

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
                attributes1.setValue("domain", domain1.getName());
                attributes1.setValue("partition", domain1.getPartition());
                attributes1.setValue("cn", cn1);
                attributes1.setValue("origGidNumber", origGidNumber1);
                attributes1.setValue("gidNumber", gidNumber1);

                Attributes attributes2 = new Attributes();
                attributes2.setValue("domain", domain2.getName());
                attributes2.setValue("partition", domain2.getPartition());
                attributes2.setValue("cn", cn2);
                attributes2.setValue("origGidNumber", origGidNumber2);
                attributes2.setValue("gidNumber", gidNumber2);

                response.add(new Conflict(attributes1, attributes2));
            }
        };

        client1.executeQuery(sql, assignments, queryResponse);
    }
}
