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
package org.safehaus.penrose.studio.module.editor;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.window.Window;
import org.safehaus.penrose.module.ModuleConfig;
import org.safehaus.penrose.module.ModuleMapping;
import org.safehaus.penrose.studio.module.wizard.ModuleMappingWizard;

import java.util.Collection;

public class ModuleMappingsPage extends FormPage {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

    Table mappingsTable;

    boolean dirty;

    ModuleEditor editor;
    ModuleConfig moduleConfig;
    Collection<ModuleMapping> moduleMappings;

    public ModuleMappingsPage(ModuleEditor editor) {
        super(editor, "MAPPINGS", "  Mappings  ");

        this.editor = editor;
        this.moduleConfig = editor.moduleConfig;
        this.moduleMappings = editor.moduleMappings;
    }

    public void createFormContent(IManagedForm managedForm) {

        toolkit = managedForm.getToolkit();

        ScrolledForm form = managedForm.getForm();
        form.setText("Module Editor");

        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        Section mappingsSection = toolkit.createSection(body, Section.TITLE_BAR | Section.EXPANDED);
        mappingsSection.setText("Mappings");
        mappingsSection.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite mappingsComponent = createMappingsControl(mappingsSection);
        mappingsSection.setClient(mappingsComponent);
	}

    public Composite createMappingsControl(final Composite parent) {

        Composite composite = toolkit.createComposite(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite leftControl = createMappingsLeftControl(composite);
        leftControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite rightControl = createMappingsRightControl(composite);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = 100;
        rightControl.setLayoutData(gd);

        return composite;
    }

	public Composite createMappingsLeftControl(final Composite parent) {

        mappingsTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        mappingsTable.setHeaderVisible(true);
        mappingsTable.setLinesVisible(true);
        mappingsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn tc = new TableColumn(mappingsTable, SWT.LEFT, 0);
        tc.setText("Base DN");
        tc.setWidth(250);

        tc = new TableColumn(mappingsTable, SWT.LEFT, 1);
        tc.setText("Scope");
        tc.setWidth(100);

        tc = new TableColumn(mappingsTable, SWT.LEFT, 2);
        tc.setText("Filter");
        tc.setWidth(200);

        return mappingsTable;
    }

    public Composite createMappingsRightControl(final Composite parent) {

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
                    ModuleMappingWizard wizard = new ModuleMappingWizard();
                    wizard.setModuleConfig(moduleConfig);

                    WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
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

    public void checkDirty() {
        editor.checkDirty();
    }

    public void setActive(boolean b) {
        super.setActive(b);
        if (b) refresh();
    }

    public void refresh() {

        if (moduleMappings != null) {
            for (ModuleMapping mapping : moduleMappings) {
                TableItem tableItem = new TableItem(mappingsTable, SWT.NONE);
                tableItem.setText(0, mapping.getBaseDn().toString());
                tableItem.setText(1, mapping.getScope() == null ? "" : mapping.getScope());
                tableItem.setText(2, mapping.getFilter() == null ? "" : mapping.getFilter());
                tableItem.setData(mapping);
            }
        }
    }
}