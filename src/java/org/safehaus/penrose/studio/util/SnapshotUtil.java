package org.safehaus.penrose.studio.util;

import org.safehaus.penrose.directory.AttributeMapping;
import org.safehaus.penrose.directory.EntryMapping;
import org.safehaus.penrose.ldap.*;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.attributeSyntax.AttributeSyntax;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class SnapshotUtil {

    public Logger log = LoggerFactory.getLogger(getClass());

    public void createSnapshot(PartitionConfig partitionConfig, LDAPClient client) throws Exception {
        createEntries(partitionConfig, client, "");
    }
    
    public void createEntries(PartitionConfig partitionConfig, LDAPClient client, String baseDn) throws Exception {
        if ("".equals(baseDn)) {
            SearchResult entry = client.find(baseDn);
            if (entry == null) return;
            
            EntryMapping entryMapping = createMapping(client, entry);
            partitionConfig.getDirectoryConfig().addEntryMapping(entryMapping);
        }

        Collection<SearchResult> children = client.getChildren(baseDn);
        for (SearchResult entry : children) {
            EntryMapping entryMapping = createMapping(client, entry);
            partitionConfig.getDirectoryConfig().addEntryMapping(entryMapping);

            createEntries(partitionConfig, client, entry.getDn().toString());
        }

    }

    public EntryMapping createMapping(LDAPClient client, SearchResult entry) throws Exception {

        Schema schema = client.getSchema();

        DN dn = entry.getDn();
        RDN rdn = dn.getRdn();

        EntryMapping entryMapping = new EntryMapping(dn);

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
                    entryMapping.addObjectClass(value.toString());
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
                entryMapping.addAttributeMapping(new AttributeMapping(name, AttributeMapping.CONSTANT, value, rdnAttr));
            }
        }

        return entryMapping;
    }

}
