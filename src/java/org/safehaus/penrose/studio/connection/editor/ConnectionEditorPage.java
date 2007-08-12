package org.safehaus.penrose.studio.connection.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.connection.ConnectionConfig;
import org.safehaus.penrose.studio.project.ProjectNode;

/**
 * @author Endi S. Dewata
 */
public class ConnectionEditorPage extends FormPage {

    public Logger log = LoggerFactory.getLogger(getClass());

    protected FormToolkit toolkit;

    protected ConnectionEditor editor;

    protected ProjectNode projectNode;
    protected PartitionConfig partitionConfig;
    protected ConnectionConfig connectionConfig;

    public ConnectionEditorPage(ConnectionEditor editor, String name, String label) {
        super(editor, name, label);

        this.editor = editor;

        projectNode = editor.getProjectNode();
        partitionConfig = editor.getPartitionConfig();
        connectionConfig = editor.getConnectionConfig();
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText(getTitle());
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

    public PartitionConfig getPartitionConfig() {
        return partitionConfig;
    }

    public void setPartitionConfig(PartitionConfig partitionConfig) {
        this.partitionConfig = partitionConfig;
    }

    public ConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    public void setProjectNode(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}
