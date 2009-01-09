package org.safehaus.penrose.studio.directory.editor;

import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.directory.EntryConfig;
import org.safehaus.penrose.studio.parameter.ParameterDialog;
import org.safehaus.penrose.studio.directory.wizard.EntryClassWizard;
import org.apache.log4j.Logger;

/**
 * @author Endi S. Dewata
 */
public class EntryClassPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Label classNameText;
    Table parametersTable;

    EntryEditor editor;
	EntryConfig entryConfig;

    public EntryClassPage(EntryEditor editor) {
        super(editor, "CLASS", "  Class  ");

        this.editor = editor;
        this.entryConfig = editor.entryConfig;
    }

    public void createFormContent(IManagedForm managedForm) {
        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Entry Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section classSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        classSection.setText("Class");
        classSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Control classControl = createClassControl(classSection);
        classSection.setClient(classControl);

        Section parametersSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        parametersSection.setText("Parameters");
        parametersSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Control parametersControl = createParametersControl(parametersSection);
        parametersSection.setClient(parametersControl);
    }

	public Composite createClassControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
		composite.setLayout(new GridLayout(3, false));

        Label classLabel = toolkit.createLabel(composite, "Class:");
        GridData gd = new GridData();
        gd.widthHint = 100;
        classLabel.setLayoutData(gd);

        classNameText = toolkit.createLabel(composite, "", SWT.NONE);
        classNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");

        gd = new GridData();
        gd.widthHint = 100;
        editButton.setLayoutData(gd);

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    EntryClassWizard wizard = new EntryClassWizard();
                    wizard.setEntryConfig(entryConfig);

                    WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
                    dialog.setPageSize(600, 300);

                    int rc = dialog.open();
                    if (rc == Window.CANCEL) return;

                    editor.store();

                    refresh();

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

    public Composite createParametersLeftControl(final Composite parent) {

        parametersTable = toolkit.createTable(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        parametersTable.setHeaderVisible(true);
        parametersTable.setLinesVisible(true);

        parametersTable.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent event) {
                try {
                    if (parametersTable.getSelectionCount() == 0) return;

                    int index = parametersTable.getSelectionIndex();
                    TableItem item = parametersTable.getSelection()[0];

                    String oldName = item.getText(0);
                    String oldValue = item.getText(1);

                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Edit parameter...");
                    dialog.setName(oldName);
                    dialog.setValue(oldValue);
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    String newName = dialog.getName();
                    String newValue = dialog.getValue();

                    if (!oldName.equals(newName)) {
                        entryConfig.removeParameter(oldName);
                    }

                    entryConfig.setParameter(newName, newValue);

                    editor.store();

                    refresh();
                    parametersTable.setSelection(index);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        TableColumn tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(250);

        tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(300);

        return parametersTable;
    }

    public Composite createParametersRightControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Button addButton = new Button(composite, SWT.PUSH);
        addButton.setText("Add");

        GridData gd = new GridData();
        gd.widthHint = 100;
        addButton.setLayoutData(gd);

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add parameter...");
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    entryConfig.setParameter(dialog.getName(), dialog.getValue());

                    editor.store();

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Button editButton = new Button(composite, SWT.PUSH);
        editButton.setText("Edit");

        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parametersTable.getSelectionCount() == 0) return;

                    int index = parametersTable.getSelectionIndex();
                    TableItem item = parametersTable.getSelection()[0];

                    String oldName = item.getText(0);
                    String oldValue = item.getText(1);

                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Edit parameter...");
                    dialog.setName(oldName);
                    dialog.setValue(oldValue);
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    String newName = dialog.getName();
                    String newValue = dialog.getValue();

                    if (!oldName.equals(newName)) {
                        entryConfig.removeParameter(oldName);
                    }

                    entryConfig.setParameter(newName, newValue);

                    editor.store();

                    refresh();
                    parametersTable.setSelection(index);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

        Button removeButton = new Button(composite, SWT.PUSH);
        removeButton.setText("Remove");

        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parametersTable.getSelectionCount() == 0) return;

                    TableItem items[] = parametersTable.getSelection();
                    for (TableItem item : items) {
                        String name = item.getText(0);
                        entryConfig.removeParameter(name);
                    }

                    editor.store();

                    refresh();

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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
        classNameText.setText(entryConfig.getEntryClass() == null ? "" : entryConfig.getEntryClass());

        parametersTable.removeAll();

        for (String name : entryConfig.getParameterNames()) {
            String value = entryConfig.getParameter(name);

            TableItem item = new TableItem(parametersTable, SWT.NONE);
            item.setText(0, name);
            item.setText(1, value);
        }
    }
}
