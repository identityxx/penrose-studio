package org.safehaus.penrose.studio.connection.editor;

import org.safehaus.penrose.studio.config.editor.ParametersPage;

/**
 * @author Endi S. Dewata
 */
public class ConnectionParametersPage extends ParametersPage {

    public ConnectionParametersPage(ConnectionEditor editor) {
        super(editor, "Connection Editor");
    }

    public void store() throws Exception {
        ((ConnectionEditor)getEditor()).store();
    }
}
