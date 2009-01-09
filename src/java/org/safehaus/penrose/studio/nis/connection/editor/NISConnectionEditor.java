package org.safehaus.penrose.studio.nis.connection.editor;

import org.safehaus.penrose.studio.connection.editor.ConnectionEditor;
import org.safehaus.penrose.studio.connection.editor.ConnectionParametersPage;

/**
 * @author Endi S. Dewata
 */
public class NISConnectionEditor extends ConnectionEditor {

    public void addPages() {
        try {
            addPage(new NISConnectionPropertiesPage(this));
            addPage(new ConnectionParametersPage(this));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
