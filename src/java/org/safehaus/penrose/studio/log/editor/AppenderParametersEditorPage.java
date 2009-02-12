package org.safehaus.penrose.studio.log.editor;

import org.safehaus.penrose.studio.config.editor.ParametersPage;

/**
 * @author Endi Sukma Dewata
 */
public class AppenderParametersEditorPage extends ParametersPage {

    public AppenderParametersEditorPage(AppenderEditor editor) {
        super(editor, "Appender Editor");
    }

    public void store() throws Exception {
        ((AppenderEditor)getEditor()).store();
    }
}
