package org.safehaus.penrose.studio.source.editor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.partition.PartitionClient;
import org.safehaus.penrose.partition.PartitionManagerClient;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.source.SourceClient;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.source.SourceManagerClient;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.config.editor.ParametersPage;
import org.safehaus.penrose.studio.server.Server;

/**
 * @author Endi Sukma Dewata
 */
public class SourceEditor extends FormEditor {

    public Logger log = Logger.getLogger(getClass());

    protected boolean dirty;

    protected Server server;
    protected String partitionName;
    protected String sourceName;

    protected SourceConfig sourceConfig;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        SourceEditorInput ei = (SourceEditorInput)input;
        server = ei.getServer();
        partitionName = ei.getPartitionName();
        sourceName = ei.getSourceName();

        try {
            PenroseClient client = server.getClient();
            PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
            PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
            SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

            SourceClient sourceClient = sourceManagerClient.getSourceClient(sourceName);
            sourceConfig = sourceClient.getSourceConfig();

        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }

        setPartName(ei.getName());

    }

    public void addPages() {
        try {
            addPage(new SourcePropertiesPage(this));
            addPage(new SourceFieldsPage(this));

            ParametersPage parametersPage = new SourceParametersPage(this);
            parametersPage.setParameters(sourceConfig.getParameters());
            addPage(parametersPage);

            addPage(new SourceBrowsePage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
        try {
            store();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void doSaveAs() {
    }

    public void rename(String name, String newName) throws Exception {

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

        sourceManagerClient.renameSource(name, newName);
        partitionClient.store();

        sourceConfig.setName(newName);

        setPartName(partitionName+"."+newName);
    }

    public void store() throws Exception {

        PenroseClient client = server.getClient();
        PartitionManagerClient partitionManagerClient = client.getPartitionManagerClient();
        PartitionClient partitionClient = partitionManagerClient.getPartitionClient(partitionName);
        SourceManagerClient sourceManagerClient = partitionClient.getSourceManagerClient();

        sourceManagerClient.updateSource(sourceName, sourceConfig);
        partitionClient.store();

        setPartName(partitionName+"."+sourceConfig.getName());
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }
}
