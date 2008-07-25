package org.safehaus.penrose.studio.util;

import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.directory.ProxyEntry;
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

        EntrySourceConfig sourceMapping = new EntrySourceConfig("DEFAULT", sourceConfig.getName());
        entryConfig.addSourceConfig(sourceMapping);

        entryConfig.setEntryClass(ProxyEntry.class.getName());

        //partitionConfig.getDirectoryConfig().addEntryConfig(entryConfig);
        partitionClient.createEntry(entryConfig);

        return entryConfig;
    }
}
