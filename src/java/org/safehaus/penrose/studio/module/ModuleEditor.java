/**
 * Copyright (c) 2000-2006, Identyx Corporation.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.safehaus.penrose.studio.module;

import java.util.Iterator;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.parameter.ParameterDialog;
import org.safehaus.penrose.partition.Partition;
import org.apache.log4j.Logger;

public class ModuleEditor extends EditorPart {

    Logger log = Logger.getLogger(getClass());

    ModuleConfig origModuleConfig;
    ModuleConfig moduleConfig;

    FormToolkit toolkit;

	Table parametersTable;
    Table mappingsTable;

    Partition partition;

    Collection mappings;

    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        ModuleEditorInput ei = (ModuleEditorInput)input;

        partition = ei.getPartition();
        origModuleConfig = ei.getModuleConfig();
        moduleConfig = (ModuleConfig)origModuleConfig.clone();

        setSite(site);
        setInput(input);
        setPartName(partition.getName()+"/"+moduleConfig.getName());
    }

    public void createPartControl(Composite parent) {
        try {
            toolkit = new FormToolkit(parent.getDisplay());

            ScrolledForm form = toolkit.createScrolledForm(parent);
            form.setText("Module Editor");

            Composite body = form.getBody();
            body.setLayout(new GridLayout());

            Section section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            section.setText("Module Editor");
            section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Composite propertiesSection = createPropertiesSection(section);
            section.setClient(propertiesSection);

            section = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
            section.setText("Parameters");
            section.setLayoutData(new GridData(GridData.FILL_BOTH));

            Composite parametersSection = createParametersSection(section);
            section.setClient(parametersSection);

            section = createMappingSection(body);
            section.setLayoutData(new GridData(GridData.FILL_BOTH));

	    } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
	}

	public Composite createPropertiesSection(Composite parent) {

		Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

		Label nameLabel = toolkit.createLabel(composite, "Name:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        nameLabel.setLayoutData(gd);

		final Text nameText = toolkit.createText(composite, "", SWT.BORDER);
        if (moduleConfig.getName() != null) nameText.setText(moduleConfig.getName());
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                moduleConfig.setName(nameText.getText());
                checkDirty();
            }
        });

		toolkit.createLabel(composite, "Class:");

		final Text classText = toolkit.createText(composite, "", SWT.BORDER);
        if (moduleConfig.getModuleClass() != null) classText.setText(moduleConfig.getModuleClass());
        classText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        classText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                moduleConfig.setModuleClass(classText.getText());
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Description:");

        final Text descriptionText = toolkit.createText(composite, "", SWT.BORDER);
        if (moduleConfig.getDescription() != null) descriptionText.setText(moduleConfig.getDescription());
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        descriptionText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                String s = descriptionText.getText();
                moduleConfig.setDescription("".equals(s) ? null : s);
                checkDirty();
            }
        });

        toolkit.createLabel(composite, "Enabled:");

        final Button enabledCheckbox = toolkit.createButton(composite, "", SWT.CHECK);
        enabledCheckbox.setSelection(moduleConfig.isEnabled());
        enabledCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        enabledCheckbox.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                moduleConfig.setEnabled(enabledCheckbox.getSelection());
                checkDirty();
            }
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });

        return composite;
    }

    public Composite createParametersSection(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

		parametersTable = toolkit.createTable(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
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
                        moduleConfig.removeParameter(oldName);
                    }

                    moduleConfig.setParameter(newName, newValue);

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

        Composite buttons = toolkit.createComposite(composite);
        buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        buttons.setLayout(new GridLayout());

        Button addButton = toolkit.createButton(buttons, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                    dialog.setText("Add parameter...");
                    dialog.open();

                    if (dialog.getAction() == ParameterDialog.CANCEL) return;

                    moduleConfig.setParameter(dialog.getName(), dialog.getValue());

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Button editButton = toolkit.createButton(buttons, "Edit", SWT.PUSH);
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
                        moduleConfig.removeParameter(oldName);
                    }

                    moduleConfig.setParameter(newName, newValue);

                    refresh();
                    parametersTable.setSelection(index);
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        Button removeButton = toolkit.createButton(buttons, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                try {
                    if (parametersTable.getSelectionCount() == 0) return;

                    TableItem items[] = parametersTable.getSelection();
                    for (int i=0; i<items.length; i++) {
                        String name = items[i].getText(0);
                        moduleConfig.removeParameter(name);
                    }

                    refresh();
                    checkDirty();

                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });

        refresh();

		return composite;
	}

    public void refresh() {
        parametersTable.removeAll();

        for (Iterator i=moduleConfig.getParameterNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            String value = moduleConfig.getParameter(name);

            TableItem item = new TableItem(parametersTable, SWT.CHECK);
            item.setText(0, name);
            item.setText(1, value);
        }
    }

	public Section createMappingSection(final Composite parent) {
        Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Mappings");

		Composite sectionClient = toolkit.createComposite(section);
        section.setClient(sectionClient);
		GridLayout layout = new GridLayout();
        layout.numColumns = 2;
		sectionClient.setLayout(layout);

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

        Collection mappings = partition.getModuleMappings(moduleConfig.getName());
        if (mappings != null) {
	        for (Iterator i=mappings.iterator(); i.hasNext(); ) {
	            ModuleMapping mapping = (ModuleMapping)i.next();
	
	            TableItem tableItem = new TableItem(mappingsTable, SWT.NONE);
	            tableItem.setText(0, mapping.getBaseDn());
	            tableItem.setText(1, mapping.getScope());
	            tableItem.setText(2, mapping.getFilter());
	            tableItem.setData(mapping);
	        }
        }

        mappingsTable.redraw();

        Composite buttons = toolkit.createComposite(sectionClient);
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
                item.dispose();
                checkDirty();
            }
        });

		return section;
	}

    public void doSave(IProgressMonitor iProgressMonitor) {
        try {
            store();
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    public void doSaveAs() {
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setFocus() {
    }

    public void store() throws Exception {

        boolean rename = !origModuleConfig.getName().equals(moduleConfig.getName());
        if (rename) {
            partition.removeModuleConfig(origModuleConfig.getName());
        }

        origModuleConfig.copy(moduleConfig);

        if (rename) {
            partition.addModuleConfig(origModuleConfig);
        }

        partition.removeModuleMapping(moduleConfig.getName());

        Item items[] = mappingsTable.getItems();
        for (int i=0; i<items.length; i++) {
            ModuleMapping mapping = (ModuleMapping)items[i].getData();
            mapping.setModuleName(moduleConfig.getName());
            partition.addModuleMapping(mapping);
        }

        setPartName(partition.getName()+"/"+moduleConfig.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.notifyChangeListeners();

        checkDirty();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void checkDirty() {
        try {
            dirty = false;

            if (!origModuleConfig.equals(moduleConfig)) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.debug(e.getMessage(), e);

        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }
}
