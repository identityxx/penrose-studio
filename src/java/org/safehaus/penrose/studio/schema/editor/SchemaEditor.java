package org.safehaus.penrose.studio.schema.editor;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.safehaus.penrose.client.PenroseClient;
import org.safehaus.penrose.schema.SchemaManagerClient;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.server.Server;

/**
 * @author Endi S. Dewata
 */
public class SchemaEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());

    Server server;

    Schema origSchema;
    Schema schema;

    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        SchemaEditorInput ei = (SchemaEditorInput)input;
        server = ei.getServer();

        try {
            PenroseClient client = server.getClient();
            SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
            origSchema = schemaManagerClient.getSchema(ei.getSchemaName());
            schema = (Schema)origSchema.clone();

        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }

        setSite(site);
        setInput(input);

        setPartName(ei.getName());
    }

    protected void addPages() {
        try {
            addPage(new SchemaPropertiesEditorPage(this));
            addPage(new AttributeTypesEditorPage(this));
            addPage(new ObjectClassesEditorPage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void doSaveAs() {
    }

    public void doSave(IProgressMonitor iProgressMonitor) {
        try {
            store();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }

    public void store() throws Exception {

        PenroseClient client = server.getClient();
        SchemaManagerClient schemaManagerClient = client.getSchemaManagerClient();
        schemaManagerClient.updateSchema(origSchema.getName(), schema);

        origSchema.copy(schema);

        setPartName("Schema - "+origSchema.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();
    }

    public boolean isDirty() {
        return dirty;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
