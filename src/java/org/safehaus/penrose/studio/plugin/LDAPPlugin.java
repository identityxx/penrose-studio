package org.safehaus.penrose.studio.plugin;

import org.safehaus.penrose.studio.source.editor.JNDISourceEditorInput;
import org.safehaus.penrose.studio.source.editor.JNDISourceEditor;
import org.safehaus.penrose.studio.source.editor.SourceEditorInput;

/**
 * @author Endi S. Dewata
 */
public class LDAPPlugin extends Plugin {

    public SourceEditorInput createSourceEditorInput() {
        return new JNDISourceEditorInput();
    }

    public String getSourceEditorClass() {
        return JNDISourceEditor.class.getName();
    }

}
