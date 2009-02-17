package org.safehaus.penrose.studio.util;

import org.safehaus.penrose.directory.EntryAttributeConfig;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.ldap.connection.LDAPConnectionClient;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.schema.attributeSyntax.AttributeSyntax;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi S. Dewata
 */
public class SnapshotUtil {

    public Logger log = LoggerFactory.getLogger(getClass());

    Server server;
    String partitionName;
    String connectionName;

    SchemaManagerClient schemaManagerClient;
    LDAPConnectionClient connectionClient;

    DN sourceDn;
    DN targetDn;
    Filter filter;
    Integer depth;

    PartitionClient partitionClient;
    DirectoryClient directoryClient;
    Schema schema;

    public SnapshotUtil() throws Exception {
    }

    public void run() throws Exception {

        PenroseClient client = server.getClient();

        schemaManagerClient = client.getSchemaManagerClient();

        connectionClient = new LDAPConnectionClient(
                client,
                partitionName,
                connectionName
        );

        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

        partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        directoryClient = partitionClient.getDirectoryClient();

        schema = connectionClient.getSchema();
        
        SearchRequest request = new SearchRequest();
        request.setDn(sourceDn);
        request.setScope(SearchRequest.SCOPE_BASE);
        request.setFilter(filter);

        SearchResponse response = new SearchResponse();

        response = connectionClient.search(request, response);

        if (response.hasNext()) {
            SearchResult entry = response.next();
            DN dn = entry.getDn().getRdn().append(targetDn);
            importEntries(entry, dn, 0);
        }

        partitionClient.store();
    }
    
    public void importEntries(SearchResult entry, DN targetDn, int level) throws Exception {

        EntryConfig entryConfig = createEntry(entry, targetDn);
        directoryClient.createEntry(entryConfig);

        if (depth != null && depth < ++level) return;

        SearchRequest request = new SearchRequest();
        request.setDn(entry.getDn());
        request.setScope(SearchRequest.SCOPE_ONE);
        request.setFilter(filter);

        SearchResponse response = new SearchResponse();

        response = connectionClient.search(request, response);

        for (SearchResult child : response.getResults()) {
            DN childDn = child.getDn().getRdn().append(targetDn);
            importEntries(child, childDn, level);
        }
    }

    public EntryConfig createEntry(SearchResult entry, DN targetDn) throws Exception {

        RDN rdn = targetDn.getRdn();

        EntryConfig entryConfig = new EntryConfig(targetDn);

        log.debug("Attributes:");
        Attributes attributes = entry.getAttributes();
        for (Attribute attribute : attributes.getAll()) {

            String name = attribute.getName();

            AttributeType attributeType = schema.getAttributeType(name);
            String syntax = attributeType == null ? null : attributeType.getSyntax();

            AttributeSyntax attributeSyntax = syntax == null ? null : schemaManagerClient.getAttributeSyntax(syntax);

            boolean binary = attributeSyntax != null && attributeSyntax.isHumanReadable();
            log.debug(" - "+name+": binary "+binary);

            if ("objectClass".equalsIgnoreCase(name)) {
                for (Object value : attribute.getValues()) {
                    entryConfig.addObjectClass(value.toString());
                }

            } else {
                for (Object value : attribute.getValues()) {

                    boolean rdnAttr = false;

                    if (!binary) {
                        String string = value.toString();
                        for (String n : rdn.getNames()) {
                            String v = (String) rdn.get(n);
                            if (name.equalsIgnoreCase(n) && string.equalsIgnoreCase(v)) {
                                rdnAttr = true;
                                break;
                            }
                        }
                    }

                    log.debug(" - "+name+": "+value);
                    entryConfig.addAttributeConfig(new EntryAttributeConfig(name, value, rdnAttr));
                }
            }
        }

        return entryConfig;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public DN getSourceDn() {
        return sourceDn;
    }

    public void setSourceDn(DN sourceDn) {
        this.sourceDn = sourceDn;
    }

    public DN getTargetDn() {
        return targetDn;
    }

    public void setTargetDn(DN targetDn) {
        this.targetDn = targetDn;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public LDAPConnectionClient getConnectionClient() {
        return connectionClient;
    }

    public void setConnectionClient(LDAPConnectionClient connectionClient) {
        this.connectionClient = connectionClient;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }
}
