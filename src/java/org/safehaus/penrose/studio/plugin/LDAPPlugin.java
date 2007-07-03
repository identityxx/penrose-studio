package org.safehaus.penrose.studio.plugin;

import org.safehaus.penrose.studio.source.editor.JNDISourceEditorInput;
import org.safehaus.penrose.studio.source.editor.JNDISourceEditor;
import org.safehaus.penrose.studio.source.editor.SourceEditorInput;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorInput;
import org.safehaus.penrose.studio.connection.editor.JNDIConnectionEditorInput;
import org.safehaus.penrose.studio.connection.editor.JNDIConnectionEditor;

/**
 * @author Endi S. Dewata
 */
public class LDAPPlugin extends Plugin {

    public ConnectionEditorInput createConnectionEditorInput() {
        return new JNDIConnectionEditorInput();
    }

    public String getConnectionEditorClass() {
        return JNDIConnectionEditor.class.getName();
    }

    public SourceEditorInput createSourceEditorInput() {
        return new JNDISourceEditorInput();
    }

    public String getSourceEditorClass() {
        return JNDISourceEditor.class.getName();
    }
}
