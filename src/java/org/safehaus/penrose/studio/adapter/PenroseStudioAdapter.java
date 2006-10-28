package org.safehaus.penrose.studio.adapter;

import org.safehaus.penrose.studio.source.editor.SourceEditor;
import org.safehaus.penrose.studio.source.wizard.SourceWizard;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditor;

import java.util.Collection;
import java.util.ArrayList;

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

    public String getSourceWizardClassName() {
        return SourceWizard.class.getName();
    }

    public Collection createConnectionEditorPages(ConnectionEditor editor) {
        return new ArrayList();
    }

    public Collection createSourceEditorPages(SourceEditor editor) {
        return new ArrayList();
    }
}
