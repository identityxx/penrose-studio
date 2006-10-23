package org.safehaus.penrose.studio.adapter;

import org.safehaus.penrose.studio.source.JDBCSourceEditor;
import org.safehaus.penrose.studio.source.wizard.JDBCSourceWizard;
import org.safehaus.penrose.studio.connection.editor.JDBCConnectionEditor;

/**
 * @author Endi S. Dewata
 */
public class PenroseStudioJDBCAdapter extends PenroseStudioAdapter {

    public PenroseStudioJDBCAdapter() {
    }

    public PenroseStudioJDBCAdapter(String name) {
        super(name);
    }

    public String getConnectionEditorClassName() {
        return JDBCConnectionEditor.class.getName();
    }

    public String getSourceEditorClassName() {
        return JDBCSourceEditor.class.getName();
    }

    public String getSourceWizardClassName() {
        return JDBCSourceWizard.class.getName();
    }
}
