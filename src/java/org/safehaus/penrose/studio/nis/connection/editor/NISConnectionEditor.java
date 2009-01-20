package org.safehaus.penrose.studio.nis.connection.editor;

import org.safehaus.penrose.studio.connection.editor.ConnectionEditor;
import org.safehaus.penrose.studio.connection.editor.ConnectionPropertiesPage;
import org.safehaus.penrose.studio.config.editor.ParametersPage;

/**
 * @author Endi S. Dewata
 */
public class NISConnectionEditor extends ConnectionEditor {

    ParametersPage parametersPage;

    public void addPages() {
        try {
            addPage(new ConnectionPropertiesPage(this));
            addPage(new NISConnectionPropertiesPage(this));

            parametersPage = new ParametersPage(this, "Connection Editor");
            parametersPage.setParameters(connectionConfig.getParameters());
            addPage(parametersPage);


        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
