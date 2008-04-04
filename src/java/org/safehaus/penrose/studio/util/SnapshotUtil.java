package org.safehaus.penrose.studio.util;

import org.safehaus.penrose.directory.AttributeMapping;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.SchemaUtil;
import org.safehaus.penrose.schema.attributeSyntax.AttributeSyntax;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.management.PenroseClient;
import org.safehaus.penrose.management.partition.PartitionManagerClient;
import org.safehaus.penrose.management.partition.PartitionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class SnapshotUtil {

    public Logger log = LoggerFactory.getLogger(getClass());

    Project project;

    public SnapshotUtil(Project project) throws Exception {
        this.project = project;
    }

    public void createSnapshot(String partitionName, LDAPClient ldapClient) throws Exception {
        PenroseClient client = project.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);

        createEntries(partitionClient, ldapClient, "");

        partitionClient.store();
    }
    
    public void createEntries(PartitionClient partitionClient, LDAPClient ldapClient, String baseDn) throws Exception {

        if ("".equals(baseDn)) {
            SearchResult entry = ldapClient.find(baseDn);
            if (entry == null) return;
            
            EntryConfig entryConfig = createMapping(ldapClient, entry);
            //partitionConfig.getDirectoryConfig().addEntryConfig(entryConfig);

            partitionClient.createEntry(entryConfig);
        }

        Collection<SearchResult> children = ldapClient.findChildren(baseDn);
        for (SearchResult entry : children) {
            EntryConfig entryConfig = createMapping(ldapClient, entry);
            //partitionConfig.getDirectoryConfig().addEntryConfig(entryConfig);
            partitionClient.createEntry(entryConfig);

            createEntries(partitionClient, ldapClient, entry.getDn().toString());
        }

    }

    public EntryConfig createMapping(LDAPClient client, SearchResult entry) throws Exception {

        SchemaUtil schemaUtil = new SchemaUtil();
        Schema schema = schemaUtil.getSchema(client);

        DN dn = entry.getDn();
        RDN rdn = dn.getRdn();

        EntryConfig entryConfig = new EntryConfig(dn);

        log.debug("Attributes:");
        Attributes attributes = entry.getAttributes();
        for (Attribute attribute : attributes.getAll()) {

            String name = attribute.getName();
            AttributeType attributeType = schema.getAttributeType(name);
            AttributeSyntax attributeSyntax = AttributeSyntax.getAttributeSyntax(attributeType.getSyntax());
            boolean binary = attributeSyntax != null && attributeSyntax.isHumanReadable();
            log.debug(" - "+name+": binary "+binary);

            boolean oc = "objectClass".equalsIgnoreCase(name);

            for (Object value : attribute.getValues()) {

                if (oc) {
                    entryConfig.addObjectClass(value.toString());
                    continue;
                }

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
                entryConfig.addAttributeMapping(new AttributeMapping(name, AttributeMapping.CONSTANT, value, rdnAttr));
            }
        }

        return entryConfig;
    }

}
