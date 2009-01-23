package org.safehaus.penrose.studio.directory.editor;

import org.safehaus.penrose.studio.config.editor.ParametersPage;

/**
 * @author Endi S. Dewata
 */
public class EntryParametersPage extends ParametersPage {

    public EntryParametersPage(EntryEditor editor) {
        super(editor, "Entry Editor");
    }

    public void store() throws Exception {
        ((EntryEditor)getEditor()).store();
    }
}