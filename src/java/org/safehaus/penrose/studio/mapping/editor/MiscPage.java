package org.safehaus.penrose.studio.mapping.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.safehaus.penrose.directory.EntryMapping;
import org.safehaus.penrose.mapping.Link;
import org.safehaus.penrose.studio.project.Project;
import org.safehaus.penrose.config.PenroseConfig;
import org.safehaus.penrose.partition.PartitionConfigs;
import org.safehaus.penrose.handler.HandlerConfig;
import org.safehaus.penrose.engine.EngineConfig;

/**
 * @author Endi S. Dewata
 */
public class MiscPage extends FormPage {

    FormToolkit toolkit;

    Combo partitionCombo;
    Combo handlerCombo;
    Combo engineCombo;

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

        Project project = editor.getProject();
        PenroseConfig penroseConfig = project.getPenroseConfig();
        PartitionConfigs partitionConfigs = project.getPartitionConfigs();

        Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(2, false));

		Label partitionLabel = toolkit.createLabel(composite, "Partition:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        partitionLabel.setLayoutData(gd);

        Link link = entryMapping.getLink();
        
        partitionCombo = new Combo(composite, SWT.BORDER);
        partitionCombo.add("");

        for (String name : partitionConfigs.getPartitionNames()) {
            partitionCombo.add(name);
        }

        if (link != null && link.getPartitionName() != null) {
            partitionCombo.setText(link.getPartitionName());
        }

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
		partitionCombo.setLayoutData(gd);

        partitionCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(partitionCombo.getText())) {
                    entryMapping.setLink(null);
                } else {
                    Link link = new Link();
                    link.setPartitionName(partitionCombo.getText());
                    entryMapping.setLink(link);
                }
                checkDirty();
            }
        });

        Label handlerLabel = toolkit.createLabel(composite, "Handler:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        handlerLabel.setLayoutData(gd);

        handlerCombo = new Combo(composite, SWT.BORDER);
        handlerCombo.add("");

        for (HandlerConfig handlerConfig : penroseConfig.getHandlerConfigs()) {
            handlerCombo.add(handlerConfig.getName());
        }

        String value = entryMapping.getHandlerName();
        if (value != null) {
            handlerCombo.setText(value);
        }

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        handlerCombo.setLayoutData(gd);

        handlerCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(handlerCombo.getText())) {
                    entryMapping.setHandlerName(null);
                } else {
                    entryMapping.setHandlerName(handlerCombo.getText());
                }
                checkDirty();
            }
        });

        Label engineLabel = toolkit.createLabel(composite, "Engine:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        engineLabel.setLayoutData(gd);

        engineCombo = new Combo(composite, SWT.BORDER);
        engineCombo.add("");

        for (EngineConfig engineConfig : penroseConfig.getEngineConfigs()) {
            engineCombo.add(engineConfig.getName());
        }

        value = entryMapping.getEngineName();
        if (value != null) {
            engineCombo.setText(value);
        }

        gd = new GridData(GridData.FILL);
        gd.widthHint = 200;
        engineCombo.setLayoutData(gd);

        engineCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if ("".equals(engineCombo.getText())) {
                    entryMapping.setEngineName(null);
                } else {
                    entryMapping.setEngineName(engineCombo.getText());
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
