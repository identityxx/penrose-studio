package org.safehaus.penrose.studio.nis.action;

import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.nis.NISDomain;
import org.safehaus.penrose.studio.nis.NISTool;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.connection.Connection;

import java.util.Collection;
import java.util.ArrayList;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class InconsistentUIDFinderAction extends NISAction {

    public InconsistentUIDFinderAction() throws Exception {
        setName("Inconsistent UID Finder");
        setDescription("Finds users with inconsistent UID numbers across domains");
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

        Project project = nisTool.getProject();
        PartitionConfigs partitionConfigs = project.getPartitionConfigs();

        PartitionConfig partitionConfig1 = partitionConfigs.getPartitionConfig(domain1.getName());
        SourceConfig sourceConfig1 = partitionConfig1.getSourceConfigs().getSourceConfig(NISTool.CACHE_USERS);

        PartitionConfig partitionConfig2 = partitionConfigs.getPartitionConfig(domain2.getName());
        SourceConfig sourceConfig2 = partitionConfig2.getSourceConfigs().getSourceConfig(NISTool.CACHE_USERS);

        Partition partition = nisTool.getNisPartition();
        Connection connection = partition.getConnection(NISTool.NIS_CONNECTION_NAME);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        JDBCClient client = adapter.getClient();

        String table1 = client.getTableName(sourceConfig1);
        String table2 = client.getTableName(sourceConfig2);

        String sql = "select a.uid, a.uidNumber, b.uidNumber, c.uid, c.uidNumber, d.uidNumber" +
                " from "+table1+" a"+
                " left join nis_cache.users b on b.domain=? and a.uid=b.uid"+
                " join "+table2+" c on a.uid = c.uid "+
                " left join nis_cache.users d on d.domain=? and c.uid=d.uid"+
                " where b.uidNumber is null and d.uidNumber is null and a.uidNumber <> c.uidNumber"+
                    " or b.uidNumber is null and a.uidNumber <> d.uidNumber"+
                    " or d.uidNumber is null and b.uidNumber <> c.uidNumber"+
                    " or b.uidNumber <> d.uidNumber" +
                " order by a.uid";

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(domain1.getName()));
        assignments.add(new Assignment(domain2.getName()));

        QueryResponse queryResponse = new QueryResponse() {
            public void add(Object object) throws Exception {
                ResultSet rs = (ResultSet)object;

                String uid1 = rs.getString(1);
                Object origUidNumber1 = rs.getObject(2);
                Object uidNumber1 = rs.getObject(3);

                String uid2 = rs.getString(4);
                Object origUidNumber2 = rs.getObject(5);
                Object uidNumber2 = rs.getObject(6);

                Attributes attributes1 = new Attributes();
                attributes1.setValue("domain", domain1.getName());
                attributes1.setValue("uid", uid1);
                attributes1.setValue("origUidNumber", origUidNumber1);
                attributes1.setValue("uidNumber", uidNumber1);

                Attributes attributes2 = new Attributes();
                attributes2.setValue("domain", domain2.getName());
                attributes2.setValue("uid", uid2);
                attributes2.setValue("origUidNumber", origUidNumber2);
                attributes2.setValue("uidNumber", uidNumber2);

                response.add(new Conflict(attributes1, attributes2));
            }
        };

        client.executeQuery(sql, assignments, queryResponse);
    }
}
