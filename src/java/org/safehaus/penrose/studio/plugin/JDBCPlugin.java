package org.safehaus.penrose.studio.plugin;

import org.safehaus.penrose.studio.jdbc.source.JDBCSourceEditorInput;
import org.safehaus.penrose.studio.jdbc.source.JDBCSourceEditor;
import org.safehaus.penrose.studio.source.editor.SourceEditorInput;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorInput;
import org.safehaus.penrose.studio.jdbc.connection.JDBCConnectionEditorInput;
import org.safehaus.penrose.studio.jdbc.connection.JDBCConnectionEditor;

/**
 * @author Endi S. Dewata
 */
public class JDBCPlugin extends Plugin {

    public ConnectionEditorInput createConnectionEditorInput() {
        return new JDBCConnectionEditorInput();
    }

    public String getConnectionEditorClass() {
        return JDBCConnectionEditor.class.getName();
    }

    public SourceEditorInput createSourceEditorInput() {
        return new JDBCSourceEditorInput();
    }

    public String getSourceEditorClass() {
        return JDBCSourceEditor.class.getName();
    }
}
