package org.safehaus.penrose.studio.util;

import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.AttributeType;
import org.safehaus.penrose.schema.attributeSyntax.AttributeSyntax;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.ldap.RDNBuilder;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.mapping.AttributeMapping;
import org.safehaus.penrose.ldap.LDAPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.directory.SearchResult;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.naming.NamingEnumeration;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * @author Endi S. Dewata
 */
public class SnapshotUtil {

    Logger log = LoggerFactory.getLogger(getClass());

    public void createSnapshot(Partition partition, LDAPClient client) throws Exception {
        createEntries(partition, client, "");
    }
    
    public void createEntries(Partition partition, LDAPClient client, String baseDn) throws Exception {
        if ("".equals(baseDn)) {
            SearchResult entry = client.getEntry(baseDn);
            if (entry == null) return;
            
            EntryMapping entryMapping = createMapping(client, entry);
            partition.getMappings().addEntryMapping(entryMapping);
        }

        Collection children = client.getChildren(baseDn);
        for (Iterator i=children.iterator(); i.hasNext(); ) {
            SearchResult entry = (SearchResult)i.next();
            EntryMapping entryMapping = createMapping(client, entry);
            partition.getMappings().addEntryMapping(entryMapping);

            createEntries(partition, client, entry.getName());
        }

    }

    public EntryMapping createMapping(LDAPClient client, SearchResult entry) throws Exception {

        Schema schema = client.getSchema();

        DN dn = entry.getName().equals("") ? client.getSuffix() : new DN(entry.getName()+","+client.getSuffix());
        RDN rdn = dn.getRdn();

        EntryMapping entryMapping = new EntryMapping(dn);

        log.debug("Attributes:");
        Attributes attributes = entry.getAttributes();
        for (NamingEnumeration i=attributes.getAll(); i.hasMore(); ) {
            Attribute attribute = (Attribute)i.next();

            String name = attribute.getID();
            AttributeType attributeType = schema.getAttributeType(name);
            AttributeSyntax attributeSyntax = AttributeSyntax.getAttributeSyntax(attributeType.getSyntax());
            boolean binary = attributeSyntax != null && attributeSyntax.isHumanReadable();
            log.debug(" - "+name+": binary "+binary);

            Collection values = new ArrayList();
            for (NamingEnumeration j=attribute.getAll(); j.hasMore(); ) {
                Object value = j.next();
                values.add(value);
            }

            if ("objectClass".equalsIgnoreCase(name)) {
                entryMapping.addObjectClasses(values);
                continue;
            }

            for (Iterator j=values.iterator(); j.hasNext(); ) {
                Object value = j.next();
                boolean rdnAttr = false;

                if (!binary) {
                    String string = value.toString();
                    for (Iterator k=rdn.getNames().iterator(); k.hasNext(); ) {
                        String n = (String)k.next();
                        String v = (String)rdn.get(n);
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
