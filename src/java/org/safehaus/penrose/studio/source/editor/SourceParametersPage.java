package org.safehaus.penrose.studio.source.editor;

import org.safehaus.penrose.studio.config.editor.ParametersPage;

/**
 * @author Endi S. Dewata
 */
public class SourceParametersPage extends ParametersPage {

    public SourceParametersPage(SourceEditor editor) {
        super(editor, "Source Editor");
    }

    public void store() throws Exception {
        ((SourceEditor)getEditor()).store();
    }
}