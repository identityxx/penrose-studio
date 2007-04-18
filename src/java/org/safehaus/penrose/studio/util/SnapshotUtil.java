package org.safehaus.penrose.studio.util;

import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.ldap.RDN;
import org.safehaus.penrose.ldap.RDNBuilder;
import org.safehaus.penrose.ldap.DN;
import org.safehaus.penrose.mapping.AttributeMapping;
import org.safehaus.penrose.ldap.LDAPClient;

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

    public void createSnapshot(Partition partition, LDAPClient client) throws Exception {
        createEntries(partition, client, "");
    }
    
    public void createEntries(Partition partition, LDAPClient client, String baseDn) throws Exception {
        if ("".equals(baseDn)) {
            SearchResult entry = client.getEntry(baseDn);
            if (entry == null) return;
            
            EntryMapping entryMapping = createMapping(client, entry);
            partition.addEntryMapping(entryMapping);
        }

        Collection children = client.getChildren(baseDn);
        for (Iterator i=children.iterator(); i.hasNext(); ) {
            SearchResult entry = (SearchResult)i.next();
            EntryMapping entryMapping = createMapping(client, entry);
            partition.addEntryMapping(entryMapping);

            createEntries(partition, client, entry.getName());
        }

    }

    public EntryMapping createMapping(LDAPClient client, SearchResult entry) throws Exception {

        Schema schema = client.getSchema();

        DN dn = new DN(entry.getName().equals("") ? client.getSuffix() : entry.getName()+","+client.getSuffix());
        RDN rdn = dn.getRdn();

        //log.debug("Creating mapping:");
        EntryMapping entryMapping = new EntryMapping(dn);
        Attributes attributes = entry.getAttributes();
        for (NamingEnumeration i=attributes.getAll(); i.hasMore(); ) {
            Attribute attribute = (Attribute)i.next();

            String name = attribute.getID();

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
                String value = j.next().toString();

                boolean rdnAttr = false;
                for (Iterator k=rdn.getNames().iterator(); k.hasNext(); ) {
                    String n = (String)k.next();
                    String v = (String)rdn.get(n);
                    if (name.equalsIgnoreCase(n) && value.equalsIgnoreCase(v)) {
                        rdnAttr = true;
                        break;
                    }
                }

                entryMapping.addAttributeMapping(new AttributeMapping(name, AttributeMapping.CONSTANT, value, rdnAttr));
            }
        }

        return entryMapping;
    }

}
