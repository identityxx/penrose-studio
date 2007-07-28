package org.safehaus.penrose.studio.connection.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.partition.PartitionConfig;
import org.safehaus.penrose.connection.ConnectionConfig;

/**
 * @author Endi S. Dewata
 */
public class JDBCConnectionEditorPage extends FormPage {

    public Logger log = LoggerFactory.getLogger(getClass());

    protected FormToolkit toolkit;

    protected PartitionConfig partitionConfig;
    protected ConnectionConfig connectionConfig;

    public JDBCConnectionEditorPage(JDBCConnectionEditor editor, String name, String label) {
        super(editor, name, label);

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

    public void checkDirty() {
        JDBCConnectionEditor editor = (JDBCConnectionEditor)getEditor();
        editor.checkDirty();
    }
}
