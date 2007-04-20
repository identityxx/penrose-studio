package org.safehaus.penrose.studio.plugin;

import org.safehaus.penrose.studio.source.editor.NISSourceEditor;
import org.safehaus.penrose.studio.connection.editor.NISConnectionEditor;

/**
 * @author Endi S. Dewata
 */
public class NISPlugin extends Plugin {

    public String getConnectionEditorClass() {
        return NISConnectionEditor.class.getName();
    }

    public String getSourceEditorClass() {
        return NISSourceEditor.class.getName();
    }

}
