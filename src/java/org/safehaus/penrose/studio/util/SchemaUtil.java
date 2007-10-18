package org.safehaus.penrose.studio.util;

import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.directory.EntryMapping;
import org.safehaus.penrose.directory.SourceMapping;

/**
 * @author Endi S. Dewata
 */
public class SchemaUtil {
    
    public EntryMapping createSchemaProxy(
            PartitionConfig partitionConfig,
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

        partitionConfig.getSourceConfigs().addSourceConfig(sourceConfig);

        EntryMapping entryMapping = new EntryMapping();
        entryMapping.setDn(destSchemaDn);

        SourceMapping sourceMapping = new SourceMapping("DEFAULT", sourceConfig.getName());
        entryMapping.addSourceMapping(sourceMapping);

        entryMapping.setHandlerName("PROXY");

        partitionConfig.getDirectoryConfig().addEntryMapping(entryMapping);

        return entryMapping;
    }
}
