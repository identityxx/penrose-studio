package org.safehaus.penrose.studio.schema;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.schema.Schema;
import org.safehaus.penrose.schema.SchemaWriter;
import org.safehaus.penrose.schema.SchemaManager;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.project.ProjectNode;
import org.safehaus.penrose.studio.project.Project;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * @author Endi S. Dewata
 */
public class SchemaEditor extends FormEditor {

    Logger log = Logger.getLogger(getClass());

    ProjectNode projectNode;

    public Schema origSchema;
    public Schema schema;

    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        SchemaEditorInput sei = (SchemaEditorInput)input;
        projectNode = sei.getProjectNode();

        origSchema = sei.getSchema();
        try {
            schema = (Schema)origSchema.clone();
        } catch (Exception e) {
            throw new PartInitException(e.getMessage(), e);
        }

        setSite(site);
        setInput(input);

        setPartName("Schema - "+schema.getName());
    }

    protected void addPages() {
        try {
            addPage(new SchemaPropertiesEditorPage(this));
            addPage(new AttributeTypesEditorPage(this));
            addPage(new ObjectClassesEditorPage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
        }
    }

    public void store() throws Exception {
        Project project = projectNode.getProject();
        SchemaManager schemaManager = project.getSchemaManager();
        schemaManager.removeSchema(origSchema.getName());

        origSchema.copy(schema);

        schemaManager.addSchema(origSchema);

        log.debug("Writing schema "+origSchema.getName()+" to "+project.getWorkDir());

        SchemaWriter schemaWriter = new SchemaWriter(project.getWorkDir());
        schemaWriter.write(origSchema);

        setPartName("Schema - "+origSchema.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();

        checkDirty();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void checkDirty() {
        try {
            dirty = false;

            if (!origSchema.equals(schema)) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
