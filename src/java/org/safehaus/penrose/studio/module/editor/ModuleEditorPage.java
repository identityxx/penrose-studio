package org.safehaus.penrose.studio.module.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.IManagedForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.module.ModuleConfig;

import java.util.Collection;

/**
 * @author Endi S. Dewata
 */
public class ModuleEditorPage extends FormPage {

    public Logger log = LoggerFactory.getLogger(getClass());

    private FormToolkit toolkit;

    private Partition partition;
    private ModuleConfig moduleConfig;
    private Collection mappings;

    public ModuleEditorPage(ModuleEditor editor, String name, String label) {
        super(editor, name, label);

        partition = editor.getPartition();
        moduleConfig = editor.getModuleConfig();
        mappings = editor.getMappings();
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText(moduleConfig.getName());
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

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }

    public void setModuleConfig(ModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }

    public Collection getMappings() {
        return mappings;
    }

    public void setMappings(Collection mappings) {
        this.mappings = mappings;
    }

    public void checkDirty() {
        ModuleEditor editor = (ModuleEditor)getEditor();
        editor.checkDirty();
    }
}
