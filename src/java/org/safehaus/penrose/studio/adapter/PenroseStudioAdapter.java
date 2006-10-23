package org.safehaus.penrose.studio.adapter;

import org.safehaus.penrose.studio.source.SourceEditor;
import org.safehaus.penrose.studio.source.wizard.SourceWizard;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditor;

/**
 * @author Endi S. Dewata
 */
public class PenroseStudioAdapter {

    String name;

    public PenroseStudioAdapter() {
    }

    public PenroseStudioAdapter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnectionEditorClassName() {
        return ConnectionEditor.class.getName();
    }

    public String getSourceEditorClassName() {
        return SourceEditor.class.getName();
    }

    public String getSourceWizardClassName() {
        return SourceWizard.class.getName();
    }
}
