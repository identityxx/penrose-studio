package org.safehaus.penrose.studio.nis.connection.editor;

import org.safehaus.penrose.studio.connection.editor.ConnectionEditor;
import org.safehaus.penrose.studio.connection.editor.ConnectionPropertiesPage;
import org.safehaus.penrose.studio.connection.editor.ConnectionParametersPage;
import org.safehaus.penrose.studio.config.editor.ParametersPage;
import org.safehaus.penrose.studio.dialog.ErrorDialog;

/**
 * @author Endi S. Dewata
 */
public class NISConnectionEditor extends ConnectionEditor {

    public void addPages() {
        try {
            addPage(new ConnectionPropertiesPage(this));
            addPage(new NISConnectionPropertiesPage(this));

            ParametersPage parametersPage = new ConnectionParametersPage(this);
            parametersPage.setParameters(connectionConfig.getParameters());
            addPage(parametersPage);


        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}
