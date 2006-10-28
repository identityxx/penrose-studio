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
package org.safehaus.penrose.studio.schema.editor;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.safehaus.penrose.studio.PenroseStudio;
import org.safehaus.penrose.studio.module.editor.ModuleMappingDialog;
import org.safehaus.penrose.studio.parameter.ParameterDialog;
import org.safehaus.penrose.schema.ObjectClass;
import org.apache.log4j.Logger;

public class ObjectClassEditor extends EditorPart implements ModifyListener, SelectionListener {

    Logger log = Logger.getLogger(getClass());

    FormToolkit toolkit;

	Text oidText;
    Text namesText;
	Text descriptionText;
    Combo typeCombo;
    Text superClassesText;
    Button obsoleteCheckbox;

	Table attributesTable;

	ObjectClass objectClass;
	
    boolean dirty;

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        ObjectClassEditorInput ei = (ObjectClassEditorInput)input;
        objectClass = ei.getObjectClass();

        setSite(site);
        setInput(input);
        setPartName("Object Class - "+objectClass.getName());
    }

    public void createPartControl(Composite parent) {
        try {
            toolkit = new FormToolkit(parent.getDisplay());

            ScrolledForm form = toolkit.createScrolledForm(parent);
            form.setText("Object Class Editor");

            form.getBody().setLayout(new GridLayout());

            Section section = createPropertiesSection(form.getBody());
            section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            section = createAttributesSection(form.getBody());
            section.setLayoutData(new GridData(GridData.FILL_BOTH));

	    } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
	}

	public Section createPropertiesSection(final Composite parent) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR|Section.EXPANDED);
		section.setText("Properties");

		Composite composite = toolkit.createComposite(section);
        section.setClient(composite);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Label oidLabel = toolkit.createLabel(composite, "OID:");
        GridData gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        oidLabel.setLayoutData(gd);

		oidText = toolkit.createText(composite, "", SWT.BORDER);
        if (objectClass.getOid() != null) oidText.setText(objectClass.getOid());
		oidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        oidText.setEnabled(false);
		oidText.addModifyListener(this);

        Label namesLabel = toolkit.createLabel(composite, "Names:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        namesLabel.setLayoutData(gd);

        StringBuffer sb = new StringBuffer();
        for (Iterator i=objectClass.getNames().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            if (sb.length() > 0) sb.append(" ");
            sb.append(name);
        }

        namesText = toolkit.createText(composite, sb.toString(), SWT.BORDER);
        namesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        namesText.setEnabled(false);
        namesText.addModifyListener(this);

        Label descriptionLabel = toolkit.createLabel(composite, "Description:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        descriptionLabel.setLayoutData(gd);

        descriptionText = toolkit.createText(composite, "", SWT.BORDER);
        if (objectClass.getDescription() != null) descriptionText.setText(objectClass.getDescription());
        descriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        descriptionText.setEnabled(false);
        descriptionText.addModifyListener(this);

        Label typeLabel = toolkit.createLabel(composite, "Type:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        typeLabel.setLayoutData(gd);

        typeCombo = new Combo(composite, SWT.READ_ONLY);
        typeCombo.add(ObjectClass.STRUCTURAL);
        typeCombo.add(ObjectClass.ABSTRACT);
        typeCombo.add(ObjectClass.AUXILIARY);
        if (objectClass.getType() != null) typeCombo.setText(objectClass.getType());
        typeCombo.setEnabled(false);
        typeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label superClassesLabel = toolkit.createLabel(composite, "Super Classes:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        superClassesLabel.setLayoutData(gd);

        sb = new StringBuffer();
        for (Iterator i=objectClass.getSuperClasses().iterator(); i.hasNext(); ) {
            String superClass = (String)i.next();
            if (sb.length() > 0) sb.append(" ");
            sb.append(superClass);
        }

        superClassesText = toolkit.createText(composite, sb.toString(), SWT.BORDER);
        superClassesText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        superClassesText.setEnabled(false);
        superClassesText.addModifyListener(this);

        Label obsoleteLabel = toolkit.createLabel(composite, "Obsolete:");
        gd = new GridData(GridData.FILL);
        gd.widthHint = 100;
        obsoleteLabel.setLayoutData(gd);

        obsoleteCheckbox = toolkit.createButton(composite, "", SWT.CHECK);
        obsoleteCheckbox.setSelection(objectClass.isObsolete());
        obsoleteCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        obsoleteCheckbox.setEnabled(false);
        obsoleteCheckbox.addSelectionListener(this);

        return section;
    }

    public Section createAttributesSection(final Composite parent) {
        Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        section.setText("Attributes");

        Composite sectionClient = toolkit.createComposite(section);
        section.setClient(sectionClient);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        sectionClient.setLayout(layout);

		Composite composite = toolkit.createComposite(sectionClient);
		GridData gd = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gd);
		composite.setLayout(new FillLayout());

		attributesTable = new Table(composite, SWT.BORDER|SWT.FULL_SELECTION);
		attributesTable.setHeaderVisible(true);
		attributesTable.setLinesVisible(true);

		TableColumn tc = new TableColumn(attributesTable, SWT.LEFT);
		tc.setText("Name");
		tc.setWidth(300);

        tc = new TableColumn(attributesTable, SWT.LEFT);
        tc.setText("Required");
        tc.setWidth(100);

        Map requiredMap = new TreeMap();
        for (Iterator i=objectClass.getRequiredAttributes().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            requiredMap.put(name, new Boolean(true));
        }

        for (Iterator i=objectClass.getOptionalAttributes().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            requiredMap.put(name, new Boolean(false));
        }

        for (Iterator i=requiredMap.keySet().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            Boolean required = (Boolean)requiredMap.get(name);

            TableItem tableItem = new TableItem(attributesTable, SWT.NONE);
            tableItem.setText(0, name);
            tableItem.setText(1, required.booleanValue() ? "yes" : "");
        }

        attributesTable.redraw();

        Composite buttons = toolkit.createComposite(sectionClient);
        gd = new GridData(GridData.FILL_VERTICAL);
        buttons.setLayoutData(gd);
        buttons.setLayout(new GridLayout());

        Button addButton = new Button(buttons, SWT.FLAT);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addButton.setEnabled(false);

        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                ParameterDialog dialog = new ParameterDialog(parent.getShell(), SWT.NONE);
                dialog.setText("Add parameter...");
                dialog.open();

                if (dialog.getAction() == ModuleMappingDialog.CANCEL) return;

                TableItem item = new TableItem(attributesTable, SWT.NONE);
                item.setText(0, dialog.getName());
                item.setText(1, dialog.getValue());
                checkDirty();
            }
        });

        Button removeButton = new Button(buttons, SWT.FLAT);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        removeButton.setEnabled(false);

        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (attributesTable.getSelectionCount() == 0) return;

                TableItem item = attributesTable.getSelection()[0];
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

        //Schema schema = PenroseStudio.getSchema();

        boolean rename = !oidText.getText().equals(objectClass.getOid());
        if (rename) {
            //schema.removeObjectClass(objectClass.getName());
        }

        objectClass.setOid(oidText.getText());

        if (rename) {
            //schema.addObjectClass(objectClass);
        }

        setPartName("Object Class - "+objectClass.getName());

        PenroseStudio penroseStudio = PenroseStudio.getInstance();
        penroseStudio.fireChangeEvent();

        checkDirty();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void checkDirty() {
        try {
            dirty = false;

            if (!oidText.getText().equals(objectClass.getOid())) {
                dirty = true;
                return;
            }

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        } finally {
            firePropertyChange(PROP_DIRTY);
        }
    }

    public void modifyText(ModifyEvent event) {
        checkDirty();
    }

    public void widgetSelected(SelectionEvent event) {
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }
}
