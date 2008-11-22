package org.safehaus.penrose.studio.plugin;

import org.safehaus.penrose.studio.ldap.source.LDAPSourceEditorInput;
import org.safehaus.penrose.studio.ldap.source.LDAPSourceEditor;
import org.safehaus.penrose.studio.source.editor.SourceEditorInput;
import org.safehaus.penrose.studio.connection.editor.ConnectionEditorInput;
import org.safehaus.penrose.studio.ldap.connection.editor.LDAPConnectionEditorInput;
import org.safehaus.penrose.studio.ldap.connection.editor.LDAPConnectionEditor;

/**
 * @author Endi S. Dewata
 */
public class LDAPPlugin extends Plugin {

    public ConnectionEditorInput createConnectionEditorInput() {
        return new LDAPConnectionEditorInput();
    }

    public String getConnectionEditorClass() {
        return LDAPConnectionEditor.class.getName();
    }

    public SourceEditorInput createSourceEditorInput() {
        return new LDAPSourceEditorInput();
    }

    public String getSourceEditorClass() {
        return LDAPSourceEditor.class.getName();
    }
}
