package org.safehaus.penrose.studio.nis.action;

import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.partition.*;
import org.safehaus.penrose.studio.federation.nis.NISRepository;
import org.safehaus.penrose.studio.federation.nis.NISFederation;
import org.safehaus.penrose.studio.federation.Federation;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.connection.Connection;

import java.util.*;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class ConflictingUIDFinderAction extends NISAction {

    public ConflictingUIDFinderAction() throws Exception {
        setName("Conflicting UID Finder");
        setDescription("Finds users from different domains with conflicting UIDs");
    }

    public void execute(
            final NISActionRequest request,
            final NISActionResponse response
    ) throws Exception {

        String domainName1 = request.getDomain();
        NISRepository domain1 = nisFederation.getRepository(domainName1);

        for (String domainName2 : nisFederation.getRepositoryNames()) {
            if (domainName1.equals(domainName2)) continue;

            NISRepository domain2 = nisFederation.getRepository(domainName2);
            execute(domain1, domain2, response);
        }

        response.close();
    }

    public void execute(
            final NISRepository domain1,
            final NISRepository domain2,
            final NISActionResponse response
    ) throws Exception {

        log.debug("Checking conflicts between "+domain1.getName()+" and "+domain2.getName()+".");

        Project project = nisFederation.getProject();
        PartitionConfigs partitionConfigs = project.getPartitionConfigs();

        PartitionConfig partitionConfig1 = partitionConfigs.getPartitionConfig(domain1.getName());
        SourceConfig sourceConfig1 = partitionConfig1.getSourceConfigs().getSourceConfig(NISFederation.CACHE_USERS);

        PartitionConfig partitionConfig2 = partitionConfigs.getPartitionConfig(domain2.getName());
        SourceConfig sourceConfig2 = partitionConfig2.getSourceConfigs().getSourceConfig(NISFederation.CACHE_USERS);

        Partition partition = nisFederation.getPartition();
        Connection connection = partition.getConnection(Federation.JDBC);
        JDBCAdapter adapter = (JDBCAdapter)connection.getAdapter();
        JDBCClient client = adapter.getClient();

        String table1 = client.getTableName(sourceConfig1);
        String table2 = client.getTableName(sourceConfig2);

        String sql = "select a.uid, a.uidNumber, b.uidNumber, c.uid, c.uidNumber, d.uidNumber" +
                " from "+table1+" a"+
                " left join "+ NISFederation.NIS_TOOL +".users b on b.domain=? and a.uid=b.uid"+
                " join "+table2+" c on a.uid <> c.uid "+
                " left join "+ NISFederation.NIS_TOOL +".users d on d.domain=? and c.uid=d.uid"+
                " where b.uidNumber is null and d.uidNumber is null and a.uidNumber = c.uidNumber"+
                    " or b.uidNumber is null and a.uidNumber = d.uidNumber"+
                    " or d.uidNumber is null and b.uidNumber = c.uidNumber"+
                    " or b.uidNumber = d.uidNumber" +
                " order by a.uid";

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(domain1.getName()));
        assignments.add(new Assignment(domain2.getName()));

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
