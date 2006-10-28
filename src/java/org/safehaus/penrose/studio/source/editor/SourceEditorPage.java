package org.safehaus.penrose.studio.source.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.IManagedForm;
import org.apache.log4j.Logger;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.server.Server;

/**
 * @author Endi S. Dewata
 */
public class SourceEditorPage extends FormPage {

    public Logger log = Logger.getLogger(getClass());

    private FormToolkit toolkit;
    private Partition partition;
    private SourceConfig sourceConfig;

    public SourceEditorPage(SourceEditor editor, String name, String label) {
        super(editor, name, label);

        partition = editor.getPartition();
        sourceConfig = editor.getSourceConfig();
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText(sourceConfig.getName());
    }

    public void setActive(boolean b) {
        super.setActive(b);
        if (b) refresh();
    }

    public void refresh() {
    }

    public void checkDirty() {
        SourceEditor editor = (SourceEditor)getEditor();
        editor.checkDirty();
    }

    public Server getServer() {
        SourceEditor editor = (SourceEditor)getEditor();
        return editor.getServer();
    }

    public FormToolkit getToolkit() {
        return toolkit;
    }

    public void setToolkit(FormToolkit toolkit) {
        this.toolkit = toolkit;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }
}
