package org.safehaus.penrose.studio.log.editor;

import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.config.wizard.ParametersWizard;
import org.safehaus.penrose.log.log4j.LayoutConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Endi S. Dewata
 */
public class LayoutEditorPage extends FormPage {

    Logger log = LoggerFactory.getLogger(getClass());

    FormToolkit toolkit;

    Label classText;
    Table parametersTable;

    AppenderEditor editor;
    LayoutConfig layoutConfig;

    public LayoutEditorPage(AppenderEditor editor) {
        super(editor, "LAYOUT", "  Layout  ");

        this.editor = editor;
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Appender Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section layoutSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        layoutSection.setText("Layout");
        layoutSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite layoutComponent = createLayoutControl(layoutSection);
        layoutSection.setClient(layoutComponent);

        Section parametersSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        parametersSection.setText("Layout Parameters");
        parametersSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control parametersControl = createParametersControl(parametersSection);
        parametersSection.setClient(parametersControl);
    }

    public Composite createLayoutControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createLayoutLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createLayoutRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createLayoutLeftControl(final Composite parent) {

		Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

		Label classLabel = toolkit.createLabel(composite, "Class:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        classLabel.setLayoutData(gd);

		classText = toolkit.createLabel(composite, "", SWT.NONE);
        classText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    public Composite createLayoutRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public Composite createParametersControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createParametersLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createParametersRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

    public Composite createParametersLeftControl(Composite parent) {

        parametersTable = toolkit.createTable(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        parametersTable.setHeaderVisible(true);
        parametersTable.setLinesVisible(true);
        parametersTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(200);

        tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(350);

        return parametersTable;
    }

    public Composite createParametersRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ParametersWizard wizard = new ParametersWizard();
                    wizard.setParameters(layoutConfig.getParameters());

                    WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    layoutConfig.removeParameters();
                    layoutConfig.setParameters(wizard.getParameters());

                    store();

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public void setActive(boolean b) {
        super.setActive(b);
        if (b) refresh();
    }

    public void refresh() {

        String className = layoutConfig.getLayoutClass();
        classText.setText(className == null ? "" : className);

        parametersTable.removeAll();

        for (String name : layoutConfig.getParameterNames()) {
            String value = layoutConfig.getParameter(name);

            TableItem ti = new TableItem(parametersTable, SWT.NONE);
            ti.setText(0, name);
            ti.setText(1, value);
        }
    }

    public void store() throws Exception {
    }

    public LayoutConfig getLayoutConfig() {
        return layoutConfig;
    }

    public void setLayoutConfig(LayoutConfig layoutConfig) {
        this.layoutConfig = layoutConfig;
    }
}