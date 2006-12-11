package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.swt.widgets.Composite;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.studio.server.Server;

/**
 * @author Endi S. Dewata
 */
public abstract class MappingDialogPage extends Composite {

    Server server;
    Partition partition;
    EntryMapping entryMapping;

    public MappingDialogPage(MappingDialog dialog, Composite composite, int style) {
        super(composite, style);

        server = dialog.getServer();
        partition = dialog.getPartition();
        entryMapping = dialog.getEntryMapping();

        init();
        refresh();
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public EntryMapping getEntryMapping() {
        return entryMapping;
    }

    public void setEntryMapping(EntryMapping entryMapping) {
        this.entryMapping = entryMapping;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void init() {
    }

    public void refresh() {
    }
}
