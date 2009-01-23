package org.safehaus.penrose.studio.config.editor;

import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.studio.config.wizard.ParametersWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Endi S. Dewata
 */
public class ParametersPage extends FormPage {

    Logger log = LoggerFactory.getLogger(getClass());

    FormToolkit toolkit;
    Table parametersTable;

    String title;
    Map<String,String> parameters;
    
    public ParametersPage(FormEditor editor, String title) {
        super(editor, "PARAMETERS", "  Parameters  ");
        this.title = title;
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText(title);

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section parametersSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        parametersSection.setText("Parameters");
        parametersSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control parametersControl = createParametersControl(parametersSection);
        parametersSection.setClient(parametersControl);
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
                    wizard.setParameters(parameters);

                    WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    parameters.clear();
                    parameters.putAll(wizard.getParameters());

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

    public Map<String,String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String,String> parameters) {
        this.parameters = parameters;
    }

    public void setActive(boolean b) {
        super.setActive(b);
        if (b) refresh();
    }

    public void refresh() {

        parametersTable.removeAll();

        for (String name : parameters.keySet()) {
            String value = parameters.get(name);

            TableItem ti = new TableItem(parametersTable, SWT.NONE);
            ti.setText(0, name);
            ti.setText(1, value);
        }
    }

    public void store() throws Exception {
    }
}