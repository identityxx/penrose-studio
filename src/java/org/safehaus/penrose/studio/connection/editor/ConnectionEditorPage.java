package org.safehaus.penrose.studio.connection.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.connection.ConnectionConfig;

/**
 * @author Endi S. Dewata
 */
public class ConnectionEditorPage extends FormPage {

    Logger log = LoggerFactory.getLogger(getClass());

    FormToolkit toolkit;

    ConnectionEditor editor;
    Partition partition;
    ConnectionConfig connectionConfig;

    public ConnectionEditorPage(ConnectionEditor editor, String name, String label) {
        super(editor, name, label);

        this.editor = editor;
        partition = editor.getPartition();
        connectionConfig = editor.getConnectionConfig();

    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("[Connection] "+partition.getName()+"/"+connectionConfig.getName());
    }

    public void setActive(boolean b) {
        super.setActive(b);
        if (b) refresh();
    }

    public void refresh() {
    }
}
