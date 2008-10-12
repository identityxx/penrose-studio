package org.safehaus.penrose.studio.federation.linking.editor;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.safehaus.penrose.federation.FederationRepositoryConfig;
import org.safehaus.penrose.studio.dialog.ErrorDialog;
import org.safehaus.penrose.studio.project.Project;

/**
 * @author Endi S. Dewata
 */
public class LinkingSettingsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Label localAttributeText;
    Label globalAttributeText;
    Label linkingStorageText;

    Label mappingNameText;
    Label mappingPrefixText;

    FederationRepositoryConfig repository;

    Project project;

    public LinkingSettingsPage(FormEditor editor, FederationRepositoryConfig repository) {
        super(editor, "LINKING", "  Linking  ");

        this.repository = repository;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Settings");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section linkSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        linkSection.setText("Identity Linking");
        linkSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control linkControl = createLinkControl(linkSection);
        linkSection.setClient(linkControl);

        new Label(body, SWT.NONE);

        Section importSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        importSection.setText("Import");
        importSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control importControl = createImportControl(importSection);
        importSection.setClient(importControl);

        refresh();
    }

    public Composite createLinkControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite leftPanel = createLinkLeftPanel(composite);
        leftPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightPanel = createLinkRightPanel(composite);
        rightPanel.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        return composite;
    }

    public Composite createLinkLeftPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label localAttributeLabel = toolkit.createLabel(composite, "Local Attribute:");
        localAttributeLabel.setLayoutData(new GridData());
        GridData gd = new GridData();
        gd.widthHint = 100;
        localAttributeLabel.setLayoutData(gd);

        localAttributeText = toolkit.createLabel(composite, "");
        localAttributeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label globalAttributeLabel = toolkit.createLabel(composite, "Global Attribute:");
        gd = new GridData();
        gd.widthHint = 100;
        globalAttributeLabel.setLayoutData(gd);

        globalAttributeText = toolkit.createLabel(composite, "");
        globalAttributeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label linkingStorageLabel = toolkit.createLabel(composite, "Link Storage:");
        gd = new GridData();
        gd.widthHint = 100;
        linkingStorageLabel.setLayoutData(gd);

        linkingStorageText = toolkit.createLabel(composite, "");
        linkingStorageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createLinkRightPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout());

        return composite;
    }

    public Composite createImportControl(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite left = createImportLeftPanel(composite);
        left.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite right = createImportRightPanel(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        right.setLayoutData(gd);

        return composite;
    }

    public Composite createImportLeftPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Label mappingNameLabel = toolkit.createLabel(composite, "Mapping Name:");
        mappingNameLabel.setLayoutData(new GridData());
        GridData gd = new GridData();
        gd.widthHint = 100;
        mappingNameLabel.setLayoutData(gd);

        mappingNameText = toolkit.createLabel(composite, "");
        mappingNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label mappingPrefixLabel = toolkit.createLabel(composite, "Mapping Prefix:");
        mappingPrefixLabel.setLayoutData(new GridData());
        gd = new GridData();
        gd.widthHint = 100;
        mappingPrefixLabel.setLayoutData(gd);

        mappingPrefixText = toolkit.createLabel(composite, "");
        mappingPrefixText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createImportRightPanel(Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        composite.setLayout(layout);

        return composite;
    }

    public void refresh() {
        try {
            for (String name : repository.getParameterNames()) {
                String value = repository.getParameter(name);

                if (name.equals(FederationRepositoryConfig.LINKING_LOCAL_ATTRIBUTE)) {
                    localAttributeText.setText(value == null ? "" : value);

                } else if (name.equals(FederationRepositoryConfig.LINKING_GLOBAL_ATTRIBUTE)) {
                    globalAttributeText.setText(value == null ? "" : value);

                } else if (name.equals(FederationRepositoryConfig.LINKING_STORAGE)) {
                    linkingStorageText.setText(value == null ? "" : value);

                } else if (name.equals(FederationRepositoryConfig.IMPORT_MAPPING_NAME)) {
                    mappingNameText.setText(value == null ? "" : value);

                } else if (name.equals(FederationRepositoryConfig.IMPORT_MAPPING_PREFIX)) {
                    mappingPrefixText.setText(value == null ? "" : value);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ErrorDialog.open(e);
        }
    }
}