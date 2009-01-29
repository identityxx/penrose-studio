package org.safehaus.penrose.studio.mapping.editor;

import org.safehaus.penrose.studio.config.editor.ParametersPage;

/**
 * @author Endi S. Dewata
 */
public class MappingParametersPage extends ParametersPage {

    public MappingParametersPage(MappingEditor editor) {
        super(editor, "Mapping Editor");
    }

    public void store() throws Exception {
        ((MappingEditor)getEditor()).store();
    }
}