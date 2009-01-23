package org.safehaus.penrose.studio.module.editor;

import org.safehaus.penrose.studio.config.editor.ParametersPage;

/**
 * @author Endi S. Dewata
 */
public class ModuleParametersPage extends ParametersPage {

    public ModuleParametersPage(ModuleEditor editor) {
        super(editor, "Module Editor");
    }

    public void store() throws Exception {
        ((ModuleEditor)getEditor()).store();
    }
}