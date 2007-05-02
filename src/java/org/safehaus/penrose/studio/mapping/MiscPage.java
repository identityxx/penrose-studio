package org.safehaus.penrose.studio.mapping;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.safehaus.penrose.mapping.EntryMapping;
import org.safehaus.penrose.mapping.Link;

/**
 * @author Endi S. Dewata
 */
public class MiscPage extends FormPage {

    FormToolkit toolkit;

    Text partitionText;
    Text handlerText;
    Text engineText;

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

		Label partitionLabel = toolkit.createLabel(composite, "Partition:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        partitionLabel.setLayoutData(gd);

        Link link = entryMapping.getLink();
        
        partitionText = toolkit.createText(composite, "", SWT.BORDER);
        if (link != null) {
            partitionText.setText(link.getPartitionName() == null ? "" : link.getPartitionName());
        }

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
		partitionText.setLayoutData(gd);

        partitionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(partitionText.getText())) {
                    entryMapping.setLink(null);
                } else {
                    Link link = new Link();
                    link.setPartitionName(partitionText.getText());
                    entryMapping.setLink(link);
                }
                checkDirty();
            }
        });

        Label handlerLabel = toolkit.createLabel(composite, "Handler:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        handlerLabel.setLayoutData(gd);

        String value = entryMapping.getHandlerName();
        value = value == null ? "" : value;
        handlerText = toolkit.createText(composite, value, SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        handlerText.setLayoutData(gd);

        handlerText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(handlerText.getText())) {
                    entryMapping.setHandlerName(null);
                } else {
                    entryMapping.setHandlerName(handlerText.getText());
                }
                checkDirty();
            }
        });

        Label engineLabel = toolkit.createLabel(composite, "Engine:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        engineLabel.setLayoutData(gd);

        value = entryMapping.getEngineName();
        value = value == null ? "" : value;
        engineText = toolkit.createText(composite, value, SWT.BORDER);

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        engineText.setLayoutData(gd);

        engineText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(engineText.getText())) {
                    entryMapping.setEngineName(null);
                } else {
                    entryMapping.setEngineName(engineText.getText());
                }
                checkDirty();
            }
        });

		return composite;
	}

    public void checkDirty() {
        editor.checkDirty();
    }
}
