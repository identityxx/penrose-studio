package org.safehaus.penrose.studio.partition.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.IManagedForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.partition.PartitionConfig;

/**
 * @author Endi S. Dewata
 */
public class PartitionEditorPage extends FormPage {

    public Logger log = LoggerFactory.getLogger(getClass());

    private FormToolkit toolkit;

    PartitionConfig partitionConfig;

    public PartitionEditorPage(PartitionEditor editor, String name, String label) {
        super(editor, name, label);

        partitionConfig = editor.getPartitionConfig();
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText(partitionConfig.getName());
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

    public void checkDirty() {
        PartitionEditor editor = (PartitionEditor)getEditor();
        editor.checkDirty();
    }
}
