package org.safehaus.penrose.studio.util;

import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.SourceMapping;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.management.partition.PartitionClient;

/**
 * @author Endi S. Dewata
 */
public class SchemaUtil {

    public SchemaUtil() {
    }

    public EntryConfig createSchemaProxy(
            PartitionClient partitionClient,
            String connectionName,
            String sourceSchemaDn,
            String destSchemaDn
    ) throws Exception {

        SourceConfig sourceConfig = new SourceConfig();
        sourceConfig.setName(connectionName+" Schema");
        sourceConfig.setConnectionName(connectionName);

        sourceConfig.setParameter("baseDn", sourceSchemaDn);
        sourceConfig.setParameter("scope", "SUBTREE");
        sourceConfig.setParameter("filter", "(objectClass=*)");

        //partitionConfig.getSourceConfigManager().addSourceConfig(sourceConfig);
        partitionClient.createSource(sourceConfig);

        EntryConfig entryConfig = new EntryConfig();
        entryConfig.setDn(destSchemaDn);

        SourceMapping sourceMapping = new SourceMapping("DEFAULT", sourceConfig.getName());
        entryConfig.addSourceMapping(sourceMapping);

        entryConfig.setHandlerName("PROXY");

        //partitionConfig.getDirectoryConfig().addEntryConfig(entryConfig);
        partitionClient.createEntry(entryConfig);

        return entryConfig;
    }
}
