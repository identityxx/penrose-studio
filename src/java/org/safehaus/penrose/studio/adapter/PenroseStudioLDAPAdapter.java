package org.safehaus.penrose.studio.adapter;

import org.safehaus.penrose.studio.source.LDAPSourceEditor;
import org.safehaus.penrose.studio.source.wizard.LDAPSourceWizard;
import org.safehaus.penrose.studio.connection.editor.LDAPConnectionEditor;

/**
 * @author Endi S. Dewata
 */
public class PenroseStudioLDAPAdapter extends PenroseStudioAdapter {

    public PenroseStudioLDAPAdapter() {
    }

    public PenroseStudioLDAPAdapter(String name) {
        super(name);
    }

    public String getConnectionEditorClassName() {
        return LDAPConnectionEditor.class.getName();
    }

    public String getSourceEditorClassName() {
        return LDAPSourceEditor.class.getName();
    }

    public String getSourceWizardClassName() {
        return LDAPSourceWizard.class.getName();
    }
}
