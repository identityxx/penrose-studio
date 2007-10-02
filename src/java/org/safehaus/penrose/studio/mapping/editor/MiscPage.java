package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.safehaus.penrose.directory.EntryMapping;

/**
 * @author Endi S. Dewata
 */
public class MiscPage extends FormPage {

    FormToolkit toolkit;

    MappingEditor editor;
	EntryMapping entryMapping;

    public MiscPage(MappingEditor editor) {
        super(editor, "MISC", "  Miscelleanous  ");

        this.editor = editor;
        this.entryMapping = editor.entry;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Entry Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Miscelleanous");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control filterCacheSection = createMiscelleanousSection(section);
        section.setClient(filterCacheSection);
	}

	public Composite createMiscelleanousSection(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(2, false));

		return composite;
	}

    public void checkDirty() {
        editor.checkDirty();
    }
}
