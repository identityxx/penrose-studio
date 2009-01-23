package org.safehaus.penrose.studio.service.editor;

import org.safehaus.penrose.studio.config.editor.ParametersPage;

/**
 * @author Endi S. Dewata
 */
public class ServiceParametersPage extends ParametersPage {

    public ServiceParametersPage(ServiceEditor editor) {
        super(editor, "Service Editor");
    }

    public void store() throws Exception {
        ((ServiceEditor)getEditor()).store();
    }
}