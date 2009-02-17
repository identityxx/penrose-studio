package org.safehaus.penrose.studio.connection.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.studio.server.Server;

/**
 * @author Endi S. Dewata
 */
public class ConnectionEditorPage extends FormPage {

    public Logger log = LoggerFactory.getLogger(getClass());

    protected FormToolkit toolkit;

    protected ConnectionEditor editor;
    String title;

    protected Server server;
    protected String partitionName;
    protected ConnectionConfig connectionConfig;

    public ConnectionEditorPage(ConnectionEditor editor, String name, String title) {
        super(editor, name, "  "+title+"  ");

        this.editor = editor;
        this.title = title;

        server = editor.getServer();
        partitionName = editor.getPartitionName();
        connectionConfig = editor.getConnectionConfig();
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Connection Editor");
    }

    public void setActive(boolean b) {
        super.setActive(b);
        if (b) refresh();
    }

    public void refresh() {
    }

    public FormToolkit getToolkit() {
        return toolkit;
    }

    public void setToolkit(FormToolkit toolkit) {
        this.toolkit = toolkit;
    }

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void checkDirty() {
    }
}
