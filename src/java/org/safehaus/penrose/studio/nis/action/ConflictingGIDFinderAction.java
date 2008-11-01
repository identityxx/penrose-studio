package org.safehaus.penrose.studio.nis.action;

import org.safehaus.penrose.connection.ConnectionManager;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.jdbc.connection.JDBCConnection;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.federation.*;

import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class ConflictingGIDFinderAction extends NISAction {

    public ConflictingGIDFinderAction() throws Exception {
        setName("Conflicting GID Finder");
        setDescription("Finds groups from different domains with conflicting GIDs");
    }

    public void execute(
            final NISActionRequest request,
            final NISActionResponse response
    ) throws Exception {

        String domainName1 = request.getDomain();
        FederationRepositoryConfig domain1 = nisFederation.getRepository(domainName1);

        for (String domainName2 : nisFederation.getRepositoryNames()) {
            if (domainName1.equals(domainName2)) continue;

            FederationRepositoryConfig domain2 = nisFederation.getRepository(domainName2);
            execute(domain1, domain2, response);
        }

        response.close();
    }

    public void execute(
            final FederationRepositoryConfig domain1,
            final FederationRepositoryConfig domain2,
            final NISActionResponse response
    ) throws Exception {

        log.debug("Checking conflicts between "+domain1.getName()+" and "+domain2.getName()+".");

        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

        PartitionClient partitionClient1 = partitionManagerClient.getPartitionClient(domain1.getName()+"_"+ NISDomain.YP);
        PartitionConfig partitionConfig1 = partitionClient1.getPartitionConfig();

        PartitionClient partitionClient2 = partitionManagerClient.getPartitionClient(domain2.getName()+"_"+ NISDomain.YP);
        PartitionConfig partitionConfig2 = partitionClient2.getPartitionConfig();

        //PartitionConfigManager partitionConfigManager = project.getPartitionConfigManager();

        //PartitionConfig partitionConfig1 = partitionConfigManager.getPartitionConfig(domain1.getName()+"_"+NISFederation.YP);
        SourceConfig sourceConfig1 = partitionConfig1.getSourceConfigManager().getSourceConfig(NISFederationClient.CACHE_GROUPS);

        //PartitionConfig partitionConfig2 = partitionConfigManager.getPartitionConfig(domain2.getName()+"_"+NISFederation.YP);
        SourceConfig sourceConfig2 = partitionConfig2.getSourceConfigManager().getSourceConfig(NISFederationClient.CACHE_GROUPS);

        Partition partition = null; // nisFederation.getPartition();
        ConnectionManager connectionManager = partition.getConnectionManager();
        JDBCConnection connection = (JDBCConnection)connectionManager.getConnection(FederationClient.JDBC);

        String table1 = connection.getTableName(sourceConfig1);
        String table2 = connection.getTableName(sourceConfig2);

        String sql = "select a.cn, a.gidNumber, b.gidNumber, c.cn, c.gidNumber, d.gidNumber" +
                " from "+table1+" a"+
                " left join "+ Federation.FEDERATION +".groups b on b.domain=? and a.cn=b.cn"+
                " join "+table2+" c on a.cn <> c.cn "+
                " left join "+ Federation.FEDERATION +".groups d on d.domain=? and c.cn=d.cn"+
                " where b.gidNumber is null and d.gidNumber is null and a.gidNumber = c.gidNumber"+
                    " or b.gidNumber is null and a.gidNumber = d.gidNumber"+
                    " or d.gidNumber is null and b.gidNumber = c.gidNumber"+
                    " or b.gidNumber = d.gidNumber" +
                " order by a.cn";

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
                attributes1.setValue("cn", cn1);
                attributes1.setValue("origGidNumber", origGidNumber1);
                attributes1.setValue("gidNumber", gidNumber1);

                Attributes attributes2 = new Attributes();
                attributes2.setValue("domain", domain2.getName());
                attributes2.setValue("cn", cn2);
                attributes2.setValue("origGidNumber", origGidNumber2);
                attributes2.setValue("gidNumber", gidNumber2);

                response.add(new Conflict(attributes1, attributes2));
            }
        };

        connection.executeQuery(
                sql,
                new Object[] { domain1.getName(), domain2.getName() },
                queryResponse
        );
    }
}
