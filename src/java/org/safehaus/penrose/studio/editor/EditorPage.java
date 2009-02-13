package org.safehaus.penrose.studio.editor;

import org.apache.log4j.Logger;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.IManagedForm;

/**
 * @author Endi Sukma Dewata
 */
public class EditorPage extends FormPage {

    public Logger log = Logger.getLogger(getClass());

    public FormToolkit toolkit;
    public String header;

    public EditorPage(Editor editor, String id, String header, String title) {
        super(editor, id, title);
        this.header = header;
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText(header);

        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void init() throws Exception {
    }

    public void setActive(boolean b) {
        super.setActive(b);
        if (b) refresh();
    }

    public void refresh() {
    }
}
