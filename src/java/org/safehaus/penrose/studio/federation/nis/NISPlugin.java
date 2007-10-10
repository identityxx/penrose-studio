package org.safehaus.penrose.studio.federation.nis;

import org.safehaus.penrose.studio.nis.source.NISSourceEditor;
import org.safehaus.penrose.studio.nis.connection.NISConnectionEditor;
import org.safehaus.penrose.studio.plugin.Plugin;

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
