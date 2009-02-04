package org.safehaus.penrose.studio.util;

import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.directory.EntrySourceConfig;
import org.safehaus.penrose.directory.ProxyEntry;
import org.safehaus.penrose.directory.DirectoryClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.acl.ACI;

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
        sourceConfig.setName(connectionName+"_schema");
        sourceConfig.setConnectionName(connectionName);

        sourceConfig.setParameter("baseDn", sourceSchemaDn);
        //sourceConfig.setParameter("scope", "SUBTREE");
        //sourceConfig.setParameter("filter", "(objectClass=*)");

        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();
        sourceManagerClient.createSource(sourceConfig);

        EntryConfig entryConfig = new EntryConfig();
        entryConfig.setDn(destSchemaDn);

        EntrySourceConfig sourceMapping = new EntrySourceConfig(sourceConfig.getName());
        entryConfig.addSourceConfig(sourceMapping);

        entryConfig.setEntryClass(ProxyEntry.class.getName());

        entryConfig.addACI(new ACI("rs"));

        DirectoryClient directoryClient = partitionClient.getDirectoryClient();
        directoryClient.createEntry(entryConfig);

        return entryConfig;
    }
}
