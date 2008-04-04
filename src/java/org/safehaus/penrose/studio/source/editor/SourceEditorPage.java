package org.safehaus.penrose.studio.source.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.IManagedForm;
import org.apache.log4j.Logger;
import org.safehaus.penrose.source.SourceConfig;
import org.safehaus.penrose.studio.project.Project;

/**
 * @author Endi Sukma Dewata
 */
public class SourceEditorPage extends FormPage {

    public Logger log = Logger.getLogger(getClass());

    protected FormToolkit toolkit;

    protected SourceEditor editor;

    protected Project project;
    protected String partitionName;
    protected SourceConfig sourceConfig;

    public SourceEditorPage(SourceEditor editor, String name, String label) {
        super(editor, name, label);

        this.editor = editor;

        project = editor.getProject();
        partitionName = editor.getPartitionName();
        sourceConfig = editor.getSourceConfig();
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

    public String getPartitionName() {
        return partitionName;
    }

    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public SourceConfig getSourceConfig() {
        return sourceConfig;
    }

    public void setSourceConfig(SourceConfig sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    public void checkDirty() {
        editor.checkDirty();
    }

    public void setDirty(boolean dirty) {
        editor.setDirty(dirty);
    }
}
