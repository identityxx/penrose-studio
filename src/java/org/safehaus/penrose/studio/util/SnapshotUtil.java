package org.safehaus.penrose.studio.util;

import org.safehaus.penrose.directory.EntryAttributeConfig;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.SchemaUtil;
import org.safehaus.penrose.schema.attributeSyntax.AttributeSyntax;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.filter.Filter;
import org.safehaus.penrose.filter.FilterEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class SnapshotUtil {

    public Logger log = LoggerFactory.getLogger(getClass());

    Server server;
    String partitionName;
    LDAPClient ldapClient;
    DN sourceDn;
    DN targetDn;
    Filter filter;
    Integer depth;

    PartitionClient partitionClient;
    DirectoryClient directoryClient;
    FilterEvaluator filterEvaluator;
    Schema schema;

    public SnapshotUtil() throws Exception {
    }

    public void run() throws Exception {

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();

        partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        directoryClient = partitionClient.getDirectoryClient();

        SchemaUtil schemaUtil = new SchemaUtil();
        schema = schemaUtil.getSchema(ldapClient);

        filterEvaluator = new FilterEvaluator();
        filterEvaluator.setSchema(schema);
        
        SearchResult entry = ldapClient.find(sourceDn);
        DN dn = entry.getDn().getRdn().append(targetDn);
        importEntries(entry, dn, 0);

        partitionClient.store();
    }
    
    public void importEntries(SearchResult entry, DN targetDn, int level) throws Exception {

        if (!filterEvaluator.eval(entry.getAttributes(), filter)) return;

        EntryConfig entryConfig = createEntry(entry, targetDn);
        directoryClient.createEntry(entryConfig);

        if (depth != null && depth < ++level) return;

        Collection<SearchResult> children = ldapClient.findChildren(entry.getDn());
        for (SearchResult child : children) {
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

            AttributeSyntax attributeSyntax = syntax == null ? null : AttributeSyntax.getAttributeSyntax(syntax);

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

    public LDAPClient getLdapClient() {
        return ldapClient;
    }

    public void setLdapClient(LDAPClient ldapClient) {
        this.ldapClient = ldapClient;
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
}
