package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.safehaus.penrose.studio.server.Server;
import org.safehaus.penrose.mapping.MappingConfig;

/**
 * @author Endi S. Dewata
 */
public class MappingEditorPage extends FormPage {

    public Logger log = LoggerFactory.getLogger(getClass());

    protected FormToolkit toolkit;

    protected MappingEditor editor;
    String title;

    protected Server server;
    protected String partitionName;
    protected MappingConfig mappingConfig;

    public MappingEditorPage(MappingEditor editor, String name, String title) {
        super(editor, name, "  "+title+"  ");

        this.editor = editor;
        this.title = title;

        server = editor.getServer();
        partitionName = editor.getPartitionName();
        mappingConfig = editor.getMappingConfig();
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Mapping Editor");
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

    public MappingConfig getMappingConfig() {
        return mappingConfig;
    }

    public void setMappingConfig(MappingConfig mappingConfig) {
        this.mappingConfig = mappingConfig;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void checkDirty() {
        editor.checkDirty();
    }
}