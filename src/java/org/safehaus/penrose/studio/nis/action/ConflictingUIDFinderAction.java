package org.safehaus.penrose.studio.nis.action;

import org.safehaus.penrose.studio.PenroseApplication;
import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.source.SourceManager;
import org.safehaus.penrose.adapter.jdbc.JDBCAdapter;
import org.safehaus.penrose.partition.PartitionManager;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.naming.PenroseContext;
import org.safehaus.penrose.ldap.Attributes;

import java.util.List;
import java.sql.ResultSet;
import java.sql.Connection;

/**
 * @author Endi S. Dewata
 */
public class ConflictingUIDFinderAction extends NISAction {

    public ConflictingUIDFinderAction() {
        setName("Conflicting UID Finder");
        setDescription("Finds users from different domains with conflicting UIDs");
    }

    public void execute(
            final NISActionRequest request,
            final NISActionResponse response
    ) throws Exception {

        List domains = request.getDomains();
        if (domains.size() < 2) throw new Exception("Please specify at least 2 domains.");

        for (int i=0; i<domains.size(); i++) {
            String partition1Name = (String)domains.get(i);

            for (int j=i+1; j<domains.size(); j++) {
                String partition2Name = (String)domains.get(j);

                execute(partition1Name, partition2Name, response);
            }
        }
    }

    public void execute(
            String partition1Name,
            String partition2Name,
            final NISActionResponse response
    ) throws Exception {

        PenroseApplication penroseApplication = PenroseApplication.getInstance();
        PenroseContext penroseContext = penroseApplication.getPenroseContext();
        PartitionManager partitionManager = penroseContext.getPartitionManager();
        SourceManager sourceManager = penroseContext.getSourceManager();

        final Partition partition1 = partitionManager.getPartition(partition1Name);
        final Source source1 = sourceManager.getSource(partition1, "users_cache");

        final Partition partition2 = partitionManager.getPartition(partition2Name);
        final Source source2 = sourceManager.getSource(partition2, "users_cache");

        JDBCAdapter adapter1 = (JDBCAdapter)source1.getConnection().getAdapter();
        JDBCClient client1 = adapter1.getClient();
        Connection connection1 = client1.getConnection();
        String catalog1 = connection1.getCatalog();
        connection1.close();
        String table1Name = catalog1+"."+source1.getParameter(JDBCClient.TABLE);

        JDBCAdapter adapter2 = (JDBCAdapter)source2.getConnection().getAdapter();
        JDBCClient client2 = adapter2.getClient();
        Connection connection2 = client2.getConnection();
        String catalog2 = connection2.getCatalog();
        connection2.close();
        String table2Name = catalog2+"."+source2.getParameter(JDBCClient.TABLE);

        String sql = "select a.uid, a.uidNumber, b.uid, b.uidNumber" +
                " from "+table1Name+" a, "+table2Name+" b" +
                " where a.uid <> b.uid and a.uidNumber = b.uidNumber" +
                " order by a.uid";

        QueryResponse queryResponse = new QueryResponse() {

            int counter = 1;

            public void add(Object object) throws Exception {
                ResultSet rs = (ResultSet)object;

                log.debug("Reading result #"+counter);
                
                Object uid1 = rs.getObject(1);
                Object uidNumber1 = rs.getObject(2);

                Object uid2 = rs.getObject(3);
                Object uidNumber2 = rs.getObject(4);

                Attributes attributes = new Attributes();

                attributes.setValue("partition1", partition1);
                attributes.setValue("source1", source1);
                attributes.setValue("uid1", uid1);
                attributes.setValue("uidNumber1", uidNumber1);

                attributes.setValue("partition2", partition2);
                attributes.setValue("source2", source2);
                attributes.setValue("uid2", uid2);
                attributes.setValue("uidNumber2", uidNumber2);

                response.add(attributes);
            }
        };

        client1.executeQuery(sql, queryResponse);
    }
}
