package org.safehaus.penrose.studio.module.editor;

import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.safehaus.penrose.studio.parameter.ParameterDialog;
import org.safehaus.penrose.module.ModuleMapping;

import java.util.Collection;
import java.util.Iterator;

public class ModulePropertiesPage extends ModuleEditorPage {

    Text nameText;
    Text classText;
    Text descriptionText;
    Button enabledCheckbox;

    Table parametersTable;
    Table mappingsTable;

    public ModulePropertiesPage(ModuleEditor editor) {
        super(editor, "PROPERTIES", "  Properties  ");
    }

    public void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Module Editor");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite propertiesSection = createPropertiesSection(section);
        section.setClient(propertiesSection);

        section = getToolkit().createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Parameters");
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite parametersSection = createParametersSection(section);
        section.setClient(parametersSection);

        section = createMappingSection(body);
        section.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    public void refresh() {
        nameText.setText(getModuleConfig().getName() == null ? "" : getModuleConfig().getName());
        classText.setText(getModuleConfig().getModuleClass() == null ? "" : getModuleConfig().getModuleClass());
        descriptionText.setText(getModuleConfig().getDescription() == null ? "" : getModuleConfig().getDescription());
        enabledCheckbox.setSelection(getModuleConfig().isEnabled());

        parametersTable.removeAll();

        for (Iterator i=getModuleConfig().getParameterNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            String value = getModuleConfig().getParameter(name);

            TableItem item = new TableItem(parametersTable, SWT.CHECK);
            item.setText(0, name);
            item.setText(1, value);
        }

        mappingsTable.removeAll();

        for (Iterator i=getMappings().iterator(); i.hasNext(); ) {
            ModuleMapping mapping = (ModuleMapping)i.next();

            TableItem tableItem = new TableItem(mappingsTable, SWT.NONE);
            tableItem.setText(0, mapping.getBaseDn());
            tableItem.setText(1, mapping.getScope());
            tableItem.setText(2, mapping.getFilter());
            tableItem.setData(mapping);
        }
    }

    public Composite createPropertiesSection(Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Label nameLabel = getToolkit().createLabel(composite, "Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        nameLabel.setLayoutData(gd);

        nameText = getToolkit().createText(composite, "", SWT.BORDER);
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getModuleConfig().setName("".equals(nameText.getText()) ? null : nameText.getText());
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "Class:");

        classText = getToolkit().createText(composite, "", SWT.BORDER);
        classText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        classText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getModuleConfig().setModuleClass("".equals(classText.getText()) ? null : classText.getText());
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "Description:");

        descriptionText = getToolkit().createText(composite, "", SWT.BORDER);
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                getModuleConfig().setDescription("".equals(descriptionText.getText()) ? null : descriptionText.getText());
                checkDirty();
            }
        });

        getToolkit().createLabel(composite, "Enabled:");

        enabledCheckbox = getToolkit().createButton(composite, "", SWT.CHECK);
        enabledCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        enabledCheckbox.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                getModuleConfig().setEnabled(enabledCheckbox.getSelection());
                checkDirty();
            }
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        return composite;
    }

    public Composite createParametersSection(final Composite parent) {

        Composite composite = getToolkit().createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        parametersTable = getToolkit().createTable(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        parametersTable.setHeaderVisible(true);
        parametersTable.setLinesVisible(true);

        parametersTable.setLayoutData(new GridData(GridData.FILL_BOTH));

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
                        getModuleConfig().removeParameter(oldName);
                    }

                    getModuleConfig().setParameter(newName, newValue);

                    refresh();
                    parametersTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        TableColumn tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Name");
        tc.setWidth(250);

        tc = new TableColumn(parametersTable, SWT.NONE);
        tc.setText("Value");
        tc.setWidth(250);

        Composite buttons = getToolkit().createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = getToolkit().createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add parameter...");
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    getModuleConfig().setParameter(dialog.getName(), dialog.getValue());

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Button editButton = getToolkit().createButton(buttons, "Edit", SWT.PUSH);
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
                        getModuleConfig().removeParameter(oldName);
                    }

                    getModuleConfig().setParameter(newName, newValue);

                    refresh();
                    parametersTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Button removeButton = getToolkit().createButton(buttons, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parametersTable.getSelectionCount() == 0) return;

                    TableItem items[] = parametersTable.getSelection();
                    for (int i=0; i<items.length; i++) {
                        String name = items[i].getText(0);
                        getModuleConfig().removeParameter(name);
                    }

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        return composite;
    }

    public Section createMappingSection(final Composite parent) {
        Section section = getToolkit().createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Mappings");

        Composite sectionClient = getToolkit().createComposite(section);
        section.setClient(sectionClient);
        sectionClient.setLayout(new GridLayout(2, false));

        mappingsTable = new Table(sectionClient, SWT.BORDER | SWT.FULL_SELECTION);
        mappingsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        mappingsTable.setHeaderVisible(true);
        mappingsTable.setLinesVisible(true);

        TableColumn tc = new TableColumn(mappingsTable, SWT.LEFT, 0);
        tc.setText("Base DN");
        tc.setWidth(250);

        tc = new TableColumn(mappingsTable, SWT.LEFT, 1);
        tc.setText("Scope");
        tc.setWidth(100);

        tc = new TableColumn(mappingsTable, SWT.LEFT, 2);
        tc.setText("Filter");
        tc.setWidth(200);

        Composite buttons = getToolkit().createComposite(sectionClient);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.FLAT);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                ModuleMapping mapping = new ModuleMapping();
                mapping.setScope("SUBTREE");
                mapping.setFilter("(objectClass=*)");

                ModuleMappingDialog dialog = new ModuleMappingDialog(parent.getShell(), SWT.NONE);
                dialog.setMapping(mapping);
                dialog.open();

                if (dialog.getAction() == ModuleMappingDialog.CANCEL) return;

                TableItem item = new TableItem(mappingsTable, SWT.NONE);
                item.setText(0, mapping.getBaseDn());
                item.setText(1, mapping.getScope());
                item.setText(2, mapping.getFilter());
                item.setData(mapping);

                getMappings().add(mapping);
                checkDirty();
            }
        });

        Button removeButton = new Button(buttons, SWT.FLAT);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (mappingsTable.getSelectionCount() == 0) return;

                TableItem item = mappingsTable.getSelection()[0];

                ModuleMapping mapping = (ModuleMapping)item.getData();
                getMappings().remove(mapping);

                item.dispose();
                checkDirty();
            }
        });

        return section;
    }
}
