package org.safehaus.penrose.studio.source.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.partition.SourceConfig;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * @author Endi S. Dewata
 */
public abstract class SourceWizard extends Wizard {

    Logger log = LoggerFactory.getLogger(getClass());

    protected Partition partition;
    protected ConnectionConfig connectionConfig;
    protected SourceConfig sourceConfig;

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig connection) {
        this.sourceConfig = connection;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
