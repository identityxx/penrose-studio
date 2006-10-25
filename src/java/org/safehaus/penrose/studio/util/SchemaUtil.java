package org.safehaus.penrose.studio.util;

import org.safehaus.penrose.partition.SourceConfig;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.mapping.SourceMapping;

/**
 * @author Endi S. Dewata
 */
public class SchemaUtil {
    
    public EntryMapping createSchemaProxy(
            Partition partition,
            ConnectionConfig connectionConfig,
            String sourceSchemaDn,
            String destSchemaDn
            ) throws Exception {

        SourceConfig sourceConfig = new SourceConfig();
        sourceConfig.setName(connectionConfig.getName()+" Schema");
        sourceConfig.setConnectionName(connectionConfig.getName());

        sourceConfig.setParameter("baseDn", sourceSchemaDn);
        sourceConfig.setParameter("scope", "SUBTREE");
        sourceConfig.setParameter("filter", "(objectClass=*)");

        partition.addSourceConfig(sourceConfig);

        EntryMapping entryMapping = new EntryMapping();
        entryMapping.setDn(destSchemaDn);

        SourceMapping sourceMapping = new SourceMapping("DEFAULT", sourceConfig.getName());
        sourceMapping.setProxy(true);
        entryMapping.addSourceMapping(sourceMapping);

        partition.addEntryMapping(entryMapping);

        return entryMapping;
    }
}
