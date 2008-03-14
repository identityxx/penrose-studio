package org.safehaus.penrose.studio.federation.nis.editor;

import org.apache.log4j.Logger;
//import org.safehaus.penrose.agent.client.FindClient;
import org.safehaus.penrose.connection.Connection;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.ldap.RDNBuilder;
import org.safehaus.penrose.ldap.SearchResult;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.Source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Endi Sukma Dewata
 */
public class UpdateFilesRunnable implements Runnable {

    Logger log = Logger.getLogger(getClass());

    Partition partition;

    SearchResult host;

    Source hosts;
    Source files;

    public UpdateFilesRunnable(Partition partition, SearchResult host, Source hosts, Source files) {
        this.partition = partition;
        this.host = host;
        this.hosts = hosts;
        this.files = files;
    }

    public void run() {
        try {
            update();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void update() throws Exception {

        Attributes attributes = host.getAttributes();

        final String hostname = (String) attributes.getValue("name");
        Integer port = (Integer) attributes.getValue("port");
        String s = (String) attributes.getValue("paths");

        Collection<String> paths = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(s, ",");
        while (st.hasMoreTokens()) paths.add(st.nextToken());

        RDNBuilder rb = new RDNBuilder();
        rb.set("hostname", hostname);

        Connection connection = files.getConnection();
        Map<String,String> parameters = connection.getParameters();
/*
        FindClient client = new FindClient(hostname, port);

        client.find(paths, parameters);

        // files.delete(new DN(rb.toRdn()));

        AgentResults<FindResult> results = new AgentResults<FindResult>() {
            public void add(FindResult result) throws Exception {
                log.debug(result.getUid()+" "+result.getGid()+" "+result.getPath());

                Attributes attr = new Attributes();
                attr.setValue("hostname", hostname);
                attr.setValue("path", result.getPath());
                attr.setValue("uidNumber", result.getUid());
                attr.setValue("gidNumber", result.getGid());

                files.add(new DN(), attr);

                totalCount++;

                if (totalCount % 100 == 0) {

                    Collection<Modification> modifications = new ArrayList<Modification>();

                    modifications.add(new Modification(
                            Modification.REPLACE,
                            new Attribute("files", ""+totalCount)
                    ));

                    hosts.modify(host.getDn(), modifications);
                }
            }
        };

        client.find(paths, results);

        Collection<Modification> modifications = new ArrayList<Modification>();

        modifications.add(new Modification(
                Modification.REPLACE,
                new Attribute("files", results.getTotalCount())
        ));

        modifications.add(new Modification(
                Modification.REPLACE,
                new Attribute("lastUpdated", new Date())
        ));

        modifications.add(new Modification(
                Modification.DELETE,
                new Attribute("status")
        ));

        hosts.modify(host.getDn(), modifications);
*/
    }
}
